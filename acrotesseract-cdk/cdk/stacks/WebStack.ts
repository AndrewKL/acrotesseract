import * as fs from 'node:fs';
import { CfnOutput, Fn, RemovalPolicy, Stack, StackProps } from 'aws-cdk-lib';
import * as apigwv2 from 'aws-cdk-lib/aws-apigatewayv2';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront';
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as route53targets from 'aws-cdk-lib/aws-route53-targets';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as s3deploy from 'aws-cdk-lib/aws-s3-deployment';
import { Construct } from 'constructs';

export interface WebStackProps extends StackProps {
  stageName: string;
  httpApi: apigwv2.IHttpApi;
  /** The Vite build output. Skipped when it doesn't exist (e.g. in unit tests). */
  frontendDistPath?: string;
  domain?: {
    domainName: string;
    hostedZoneId: string;
    certificate: acm.ICertificate;
  };
}

/**
 * One CloudFront origin for everything: the React SPA from S3 and /api/* from the HTTP API.
 * Same-origin means no CORS and a first-party session cookie.
 */
export class WebStack extends Stack {
  public readonly distribution: cloudfront.Distribution;

  constructor(scope: Construct, id: string, props: WebStackProps) {
    super(scope, id, props);

    const siteBucket = new s3.Bucket(this, 'SiteBucket', {
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      enforceSSL: true,
      removalPolicy: RemovalPolicy.DESTROY,
      autoDeleteObjects: true,
    });

    // Serve index.html for client-side routes (/poses/<id>) without masking API 404s, which a distribution-wide
    // error response would do. With a custom domain, also send www.<domain> to the bare domain so there's one URL.
    const apex = props.domain?.domainName;
    const wwwRedirect = apex
      ? `
  var host = request.headers.host ? request.headers.host.value : '';
  if (host === 'www.${apex}') {
    var qs = Object.keys(request.querystring).map(function (k) {
      return k + '=' + request.querystring[k].value;
    }).join('&');
    return {
      statusCode: 301,
      statusDescription: 'Moved Permanently',
      headers: { location: { value: 'https://${apex}' + request.uri + (qs ? '?' + qs : '') } },
    };
  }`
      : '';
    const spaRewrite = new cloudfront.Function(this, 'SpaRewriteFunction', {
      runtime: cloudfront.FunctionRuntime.JS_2_0,
      code: cloudfront.FunctionCode.fromInline(`
function handler(event) {
  var request = event.request;${wwwRedirect}
  if (!request.uri.includes('.')) {
    request.uri = '/index.html';
  }
  return request;
}`),
    });

    // apiEndpoint is https://<id>.execute-api.<region>.amazonaws.com; CloudFront wants the bare host.
    const apiHost = Fn.select(2, Fn.split('/', props.httpApi.apiEndpoint));

    this.distribution = new cloudfront.Distribution(this, 'SiteDistribution', {
      comment: `acrotesseract-${props.stageName}`,
      defaultRootObject: 'index.html',
      defaultBehavior: {
        origin: origins.S3BucketOrigin.withOriginAccessControl(siteBucket),
        viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
        functionAssociations: [
          { function: spaRewrite, eventType: cloudfront.FunctionEventType.VIEWER_REQUEST },
        ],
      },
      additionalBehaviors: {
        '/api/*': {
          origin: new origins.HttpOrigin(apiHost),
          viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
          allowedMethods: cloudfront.AllowedMethods.ALLOW_ALL,
          cachePolicy: cloudfront.CachePolicy.CACHING_DISABLED,
          originRequestPolicy: cloudfront.OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
        },
      },
      domainNames: props.domain
        ? [props.domain.domainName, `www.${props.domain.domainName}`]
        : undefined,
      certificate: props.domain?.certificate,
    });

    if (props.frontendDistPath && fs.existsSync(props.frontendDistPath)) {
      new s3deploy.BucketDeployment(this, 'DeploySite', {
        sources: [s3deploy.Source.asset(props.frontendDistPath)],
        destinationBucket: siteBucket,
        distribution: this.distribution,
        distributionPaths: ['/*'],
      });
    }

    if (props.domain) {
      const zone = route53.HostedZone.fromHostedZoneAttributes(this, 'HostedZone', {
        hostedZoneId: props.domain.hostedZoneId,
        zoneName: props.domain.domainName,
      });
      const target = route53.RecordTarget.fromAlias(
        new route53targets.CloudFrontTarget(this.distribution),
      );
      // IPv4 and IPv6 aliases for the bare domain and www (www is redirected to the bare domain above).
      new route53.ARecord(this, 'ApexRecord', { zone, target });
      new route53.AaaaRecord(this, 'ApexAaaaRecord', { zone, target });
      new route53.ARecord(this, 'WwwRecord', { zone, recordName: 'www', target });
      new route53.AaaaRecord(this, 'WwwAaaaRecord', { zone, recordName: 'www', target });
    }

    new CfnOutput(this, 'SiteUrl', {
      value: props.domain
        ? `https://${props.domain.domainName}`
        : `https://${this.distribution.distributionDomainName}`,
    });
  }
}
