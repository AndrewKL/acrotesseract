import { CfnOutput, RemovalPolicy, Stack, StackProps } from 'aws-cdk-lib';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import { Construct } from 'constructs';

export interface StorageStackProps extends StackProps {
  stageName: string;
}

const common = {
  billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
  pointInTimeRecoverySpecification: { pointInTimeRecoveryEnabled: true },
  deletionProtection: true,
  removalPolicy: RemovalPolicy.RETAIN,
};

const str = (name: string) => ({ name, type: dynamodb.AttributeType.STRING });

/**
 * The poses and transitions tables. The schema must match DynamoDbTables in acrotesseract-backend's store module,
 * which the backend tests use against DynamoDB Local.
 */
export class StorageStack extends Stack {
  public readonly posesTable: dynamodb.Table;
  public readonly transitionsTable: dynamodb.Table;

  constructor(scope: Construct, id: string, props: StorageStackProps) {
    super(scope, id, props);

    this.posesTable = new dynamodb.Table(this, 'PosesTable', {
      ...common,
      tableName: `acrotesseract-poses-${props.stageName}`,
      partitionKey: str('poseId'),
    });
    // Unique-name check on create and rename.
    this.posesTable.addGlobalSecondaryIndex({
      indexName: 'byName',
      partitionKey: str('nameLower'),
      projectionType: dynamodb.ProjectionType.KEYS_ONLY,
    });

    this.transitionsTable = new dynamodb.Table(this, 'TransitionsTable', {
      ...common,
      tableName: `acrotesseract-transitions-${props.stageName}`,
      partitionKey: str('transitionId'),
    });
    this.transitionsTable.addGlobalSecondaryIndex({
      indexName: 'byName',
      partitionKey: str('nameLower'),
      projectionType: dynamodb.ProjectionType.KEYS_ONLY,
    });
    // Transitions leaving / arriving at a pose, sorted by name.
    this.transitionsTable.addGlobalSecondaryIndex({
      indexName: 'byPoseFrom',
      partitionKey: str('poseFrom'),
      sortKey: str('nameLower'),
    });
    this.transitionsTable.addGlobalSecondaryIndex({
      indexName: 'byPoseTo',
      partitionKey: str('poseTo'),
      sortKey: str('nameLower'),
    });

    new CfnOutput(this, 'PosesTableName', { value: this.posesTable.tableName });
    new CfnOutput(this, 'TransitionsTableName', { value: this.transitionsTable.tableName });
  }
}
