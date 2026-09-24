import { Annotations, CfnOutput, Duration, Stack, StackProps } from 'aws-cdk-lib';
import * as apigwv2 from 'aws-cdk-lib/aws-apigatewayv2';
import { HttpLambdaIntegration } from 'aws-cdk-lib/aws-apigatewayv2-integrations';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as logs from 'aws-cdk-lib/aws-logs';
import { Construct } from 'constructs';

export interface ApiStackProps extends StackProps {
  stageName: string;
  table: dynamodb.ITable;
  /** The assembled Scala JAR in the app; a stub asset in tests. */
  lambdaCode: lambda.Code;
}

/** The Scala Lambda (Java 21, arm64, SnapStart) behind an API Gateway HTTP API. */
export class ApiStack extends Stack {
  public readonly httpApi: apigwv2.HttpApi;
  public readonly apiFunction: lambda.Function;

  constructor(scope: Construct, id: string, props: ApiStackProps) {
    super(scope, id, props);

    const logGroup = new logs.LogGroup(this, 'ApiLogGroup', {
      retention: logs.RetentionDays.ONE_MONTH,
    });

    this.apiFunction = new lambda.Function(this, 'ApiFunction', {
      runtime: lambda.Runtime.JAVA_21,
      architecture: lambda.Architecture.ARM_64,
      handler: 'acrotesseract.Handler::handleRequest',
      code: props.lambdaCode,
      memorySize: 1024,
      timeout: Duration.seconds(15),
      snapStart: lambda.SnapStartConf.ON_PUBLISHED_VERSIONS,
      logGroup,
      environment: {
        STAGE: props.stageName,
        TABLE_NAME: props.table.tableName,
        SSM_PREFIX: `/acrotesseract/${props.stageName}/`,
      },
    });

    props.table.grantReadWriteData(this.apiFunction);

    // Cookie-signing secret and other SecureStrings, created out of band with `aws ssm put-parameter`.
    this.apiFunction.addToRolePolicy(
      new iam.PolicyStatement({
        actions: ['ssm:GetParameter', 'ssm:GetParametersByPath'],
        resources: [
          this.formatArn({
            service: 'ssm',
            resource: 'parameter',
            resourceName: `acrotesseract/${props.stageName}/*`,
          }),
        ],
      }),
    );

    // SnapStart applies to published versions, so API Gateway invokes the `live` alias.
    const alias = new lambda.Alias(this, 'ApiFunctionLiveAlias', {
      aliasName: 'live',
      version: this.apiFunction.currentVersion,
    });
    Annotations.of(this.apiFunction).acknowledgeWarning(
      '@aws-cdk/aws-lambda:snapStartRequirePublish',
      'Every deploy publishes a version through the live alias.',
    );

    this.httpApi = new apigwv2.HttpApi(this, 'HttpApi', {
      apiName: `acrotesseract-api-${props.stageName}`,
      defaultIntegration: new HttpLambdaIntegration('ApiIntegration', alias),
    });

    new CfnOutput(this, 'ApiEndpoint', { value: this.httpApi.apiEndpoint });
  }
}
