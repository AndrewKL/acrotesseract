import * as cdk from 'aws-cdk-lib';
import { Template } from 'aws-cdk-lib/assertions';
import { CertStack } from './CertStack';

test('issues a DNS-validated certificate for the apex and www', () => {
  const app = new cdk.App();
  const stack = new CertStack(app, 'acrotesseract-cert-stack-test', {
    env: { account: '111111111111', region: 'us-east-1' },
    domainName: 'example.com',
    hostedZoneId: 'Z0000000000000',
  });

  Template.fromStack(stack).hasResourceProperties('AWS::CertificateManager::Certificate', {
    DomainName: 'example.com',
    SubjectAlternativeNames: ['www.example.com'],
    ValidationMethod: 'DNS',
  });
});
