import * as cdk from 'aws-cdk-lib';
import { Match, Template } from 'aws-cdk-lib/assertions';
import * as apigwv2 from 'aws-cdk-lib/aws-apigatewayv2';
import { WebStack } from './WebStack';
import { testEnv } from './testEnv';

test('serves the SPA from S3 and /api/* from the HTTP API, uncached', () => {
  const app = new cdk.App();
  const apiStack = new cdk.Stack(app, 'api', { env: testEnv });
  const httpApi = new apigwv2.HttpApi(apiStack, 'HttpApi');
  const stack = new WebStack(app, 'acrotesseract-web-stack-test', {
    env: testEnv,
    stageName: 'test',
    httpApi,
  });
  const template = Template.fromStack(stack);

  template.hasResourceProperties('AWS::CloudFront::Distribution', {
    DistributionConfig: Match.objectLike({
      DefaultRootObject: 'index.html',
      CacheBehaviors: [
        Match.objectLike({
          PathPattern: '/api/*',
          ViewerProtocolPolicy: 'redirect-to-https',
          // Managed CachingDisabled policy.
          CachePolicyId: '4135ea2d-6df8-44a3-9df3-4b5a84be39ad',
        }),
      ],
    }),
  });
  template.resourceCountIs('AWS::CloudFront::Function', 1);
  // No frontend dist was given, so nothing is uploaded.
  template.resourceCountIs('Custom::CDKBucketDeployment', 0);
});
