import { CfnOutput, RemovalPolicy, Stack, StackProps } from 'aws-cdk-lib';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import { Construct } from 'constructs';

export interface StorageStackProps extends StackProps {
  stageName: string;
}

/**
 * The single DynamoDB table (see docs/acrotesseract-modernization.md, "Data model").
 * Only META items carry GSI keys, so the indexes stay sparse.
 */
export class StorageStack extends Stack {
  public readonly table: dynamodb.Table;

  constructor(scope: Construct, id: string, props: StorageStackProps) {
    super(scope, id, props);

    this.table = new dynamodb.Table(this, 'AcroTesseractTable', {
      tableName: `acrotesseract-${props.stageName}`,
      partitionKey: { name: 'PK', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'SK', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      pointInTimeRecoverySpecification: { pointInTimeRecoveryEnabled: true },
      deletionProtection: true,
      removalPolicy: RemovalPolicy.RETAIN,
    });

    // byType: list all poses / transitions sorted by name, and the graph payload.
    this.table.addGlobalSecondaryIndex({
      indexName: 'GSI1',
      partitionKey: { name: 'GSI1PK', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'GSI1SK', type: dynamodb.AttributeType.STRING },
      projectionType: dynamodb.ProjectionType.INCLUDE,
      nonKeyAttributes: ['name', 'poseFrom', 'poseTo', 'imageUrl'],
    });

    // byFrom: transitions leaving a pose.
    this.table.addGlobalSecondaryIndex({
      indexName: 'GSI2',
      partitionKey: { name: 'GSI2PK', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'GSI2SK', type: dynamodb.AttributeType.STRING },
    });

    // byTo: transitions arriving at a pose.
    this.table.addGlobalSecondaryIndex({
      indexName: 'GSI3',
      partitionKey: { name: 'GSI3PK', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'GSI3SK', type: dynamodb.AttributeType.STRING },
    });

    new CfnOutput(this, 'TableName', { value: this.table.tableName });
  }
}
