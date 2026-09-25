import * as cdk from 'aws-cdk-lib';
import { Match, Template } from 'aws-cdk-lib/assertions';
import { StorageStack } from './StorageStack';
import { testEnv } from './testEnv';

function synth() {
  const app = new cdk.App();
  const stack = new StorageStack(app, 'acrotesseract-storage-stack-test', { env: testEnv, stageName: 'test' });
  return Template.fromStack(stack);
}

const indexNames = (template: Template, tableName: string) => {
  const table = Object.values(template.findResources('AWS::DynamoDB::Table')).find(
    (t) => t.Properties.TableName === tableName,
  );
  return table?.Properties.GlobalSecondaryIndexes.map((gsi: { IndexName: string }) => gsi.IndexName);
};

test('creates retained, protected poses and transitions tables', () => {
  const template = synth();
  template.resourceCountIs('AWS::DynamoDB::Table', 2);
  for (const [name, key] of [
    ['acrotesseract-poses-test', 'poseId'],
    ['acrotesseract-transitions-test', 'transitionId'],
  ]) {
    template.hasResourceProperties('AWS::DynamoDB::Table', {
      TableName: name,
      BillingMode: 'PAY_PER_REQUEST',
      KeySchema: [{ AttributeName: key, KeyType: 'HASH' }],
      PointInTimeRecoverySpecification: { PointInTimeRecoveryEnabled: true },
      DeletionProtectionEnabled: true,
    });
  }
  template.allResources('AWS::DynamoDB::Table', { DeletionPolicy: 'Retain', UpdateReplacePolicy: 'Retain' });
});

test('indexes match what the Scala repository queries', () => {
  const template = synth();
  expect(indexNames(template, 'acrotesseract-poses-test')).toEqual(['byName']);
  expect(indexNames(template, 'acrotesseract-transitions-test')).toEqual(['byName', 'byPoseFrom', 'byPoseTo']);
  template.hasResourceProperties('AWS::DynamoDB::Table', {
    TableName: 'acrotesseract-transitions-test',
    GlobalSecondaryIndexes: Match.arrayWith([
      Match.objectLike({
        IndexName: 'byPoseFrom',
        KeySchema: [
          { AttributeName: 'poseFrom', KeyType: 'HASH' },
          { AttributeName: 'nameLower', KeyType: 'RANGE' },
        ],
        Projection: { ProjectionType: 'ALL' },
      }),
    ]),
  });
});
