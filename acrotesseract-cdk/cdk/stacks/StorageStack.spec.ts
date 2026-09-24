import * as cdk from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import { StorageStack } from './StorageStack';
import { testEnv } from './testEnv';

test('creates the retained single table with three GSIs', () => {
  const app = new cdk.App();
  const stack = new StorageStack(app, 'acrotesseract-storage-stack-test', {
    env: testEnv,
    stageName: 'test',
  });
  const template = Template.fromStack(stack);

  template.hasResourceProperties('AWS::DynamoDB::Table', {
    TableName: 'acrotesseract-test',
    BillingMode: 'PAY_PER_REQUEST',
    KeySchema: [
      { AttributeName: 'PK', KeyType: 'HASH' },
      { AttributeName: 'SK', KeyType: 'RANGE' },
    ],
    PointInTimeRecoverySpecification: { PointInTimeRecoveryEnabled: true },
    DeletionProtectionEnabled: true,
  });
  template.hasResource('AWS::DynamoDB::Table', { DeletionPolicy: 'Retain' });

  const table = Object.values(template.findResources('AWS::DynamoDB::Table'))[0];
  const indexNames = table.Properties.GlobalSecondaryIndexes.map(
    (gsi: { IndexName: string }) => gsi.IndexName,
  );
  expect(indexNames).toEqual(['GSI1', 'GSI2', 'GSI3']);
});
