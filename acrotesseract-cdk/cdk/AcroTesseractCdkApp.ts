import * as path from 'node:path';
import * as cdk from 'aws-cdk-lib';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import { acroTesseractStages } from './AcroTesseractStages';
import { StorageStack } from './stacks/StorageStack';
import { ApiStack } from './stacks/ApiStack';
import { CertStack } from './stacks/CertStack';
import { WebStack } from './stacks/WebStack';

const repoRoot = path.resolve(__dirname, '..', '..');
const lambdaJar = path.join(
  repoRoot,
  'acrotesseract-backend/modules/lambda/target/scala-3.9.0/acrotesseract-lambda.jar',
);
const frontendDist = path.join(repoRoot, 'acrotesseract-frontend/dist');

const app = new cdk.App();

for (const stage of acroTesseractStages) {
  const env = { account: stage.accountId, region: stage.region };
  const { stageName } = stage;

  const storage = new StorageStack(app, `acrotesseract-storage-stack-${stageName}`, {
    env,
    stageName,
  });

  const api = new ApiStack(app, `acrotesseract-api-stack-${stageName}`, {
    env,
    stageName,
    table: storage.table,
    lambdaCode: lambda.Code.fromAsset(lambdaJar),
  });

  // CloudFront only accepts ACM certificates from us-east-1.
  const cert = stage.domain
    ? new CertStack(app, `acrotesseract-cert-stack-${stageName}`, {
        env: { account: stage.accountId, region: 'us-east-1' },
        crossRegionReferences: true,
        domainName: stage.domain.domainName,
        hostedZoneId: stage.domain.hostedZoneId,
      })
    : undefined;

  new WebStack(app, `acrotesseract-web-stack-${stageName}`, {
    env,
    crossRegionReferences: cert !== undefined,
    stageName,
    httpApi: api.httpApi,
    frontendDistPath: frontendDist,
    domain:
      stage.domain && cert
        ? { ...stage.domain, certificate: cert.certificate }
        : undefined,
  });
}
