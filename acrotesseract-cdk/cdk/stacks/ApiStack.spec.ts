import * as fs from 'node:fs';
import * as os from 'node:os';
import * as path from 'node:path';
import * as cdk from 'aws-cdk-lib';
import { Match, Template } from 'aws-cdk-lib/assertions';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import { ApiStack } from './ApiStack';
import { StorageStack } from './StorageStack';
import { testEnv } from './testEnv';

// Java runtimes can't use inline code, so point the function at a throwaway asset directory.
const stubAssetDir = fs.mkdtempSync(path.join(os.tmpdir(), 'acrotesseract-lambda-'));
fs.writeFileSync(path.join(stubAssetDir, 'stub.txt'), 'stub');

function synth() {
  const app = new cdk.App();
  const storage = new StorageStack(app, 'storage', { env: testEnv, stageName: 'test' });
  const stack = new ApiStack(app, 'acrotesseract-api-stack-test', {
    env: testEnv,
    stageName: 'test',
    posesTable: storage.posesTable,
    transitionsTable: storage.transitionsTable,
    writesEnabled: false,
    lambdaCode: lambda.Code.fromAsset(stubAssetDir),
  });
  return Template.fromStack(stack);
}

test('runs the Scala handler on Java 21 arm64 with SnapStart', () => {
  synth().hasResourceProperties('AWS::Lambda::Function', {
    Runtime: 'java21',
    Architectures: ['arm64'],
    Handler: 'acrotesseract.Handler::handleRequest',
    MemorySize: 1024,
    SnapStart: { ApplyOn: 'PublishedVersions' },
    Environment: {
      Variables: Match.objectLike({
        STAGE: 'test',
        POSES_TABLE: { 'Fn::ImportValue': Match.stringLikeRegexp('PosesTable') },
        TRANSITIONS_TABLE: { 'Fn::ImportValue': Match.stringLikeRegexp('TransitionsTable') },
        WRITES_ENABLED: 'false',
      }),
    },
  });
});

test('routes the HTTP API to the live alias', () => {
  const template = synth();
  template.hasResourceProperties('AWS::Lambda::Alias', { Name: 'live' });
  template.hasResourceProperties('AWS::ApiGatewayV2::Api', {
    Name: 'acrotesseract-api-test',
    ProtocolType: 'HTTP',
  });
});

test('grants data access to both tables only, not table management', () => {
  const policies = synth().findResources('AWS::IAM::Policy');
  const actions = JSON.stringify(policies);
  expect(actions).toContain('dynamodb:PutItem');
  expect(actions).toContain('dynamodb:ConditionCheckItem');
  expect(actions).toMatch(/PosesTable/);
  expect(actions).toMatch(/TransitionsTable/);
  expect(actions).not.toContain('dynamodb:CreateTable');
  expect(actions).not.toContain('dynamodb:DeleteTable');
});
