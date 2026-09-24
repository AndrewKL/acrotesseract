import { Stack, StackProps } from 'aws-cdk-lib';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as route53 from 'aws-cdk-lib/aws-route53';
import { Construct } from 'constructs';

export interface CertStackProps extends StackProps {
  domainName: string;
  hostedZoneId: string;
}

/** DNS-validated certificate for CloudFront. Must be deployed to us-east-1. */
export class CertStack extends Stack {
  public readonly certificate: acm.ICertificate;

  constructor(scope: Construct, id: string, props: CertStackProps) {
    super(scope, id, props);

    const hostedZone = route53.HostedZone.fromHostedZoneAttributes(this, 'HostedZone', {
      hostedZoneId: props.hostedZoneId,
      zoneName: props.domainName,
    });

    this.certificate = new acm.Certificate(this, 'SiteCertificate', {
      domainName: props.domainName,
      subjectAlternativeNames: [`www.${props.domainName}`],
      validation: acm.CertificateValidation.fromDns(hostedZone),
    });
  }
}
