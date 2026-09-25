export interface AcroTesseractStage {
  /** Used in stack names (`acrotesseract-<purpose>-stack-<stageName>`), the table name and SSM paths. */
  stageName: string;
  accountId: string;
  region: string;
  /** Allow POST/PUT on the API. Keep false on public stages until Google sign-in exists. */
  writesEnabled: boolean;
  /** Optional custom domain. Without it the site is served from the CloudFront default domain. */
  domain?: {
    domainName: string;
    hostedZoneId: string;
  };
}

/** The acro-tesseract-prod account in the ferrocene AWS organization (CLI profile `acrotesseract-prod`). */
const prod: AcroTesseractStage = {
  stageName: 'prod',
  accountId: '640110193230',
  region: 'us-west-2',
  writesEnabled: false,
  // Registered at GoDaddy; its nameservers point at this Route 53 zone (created with the CLI, not CDK).
  domain: { domainName: 'acrotesseract.com', hostedZoneId: 'Z101167449K0416WE8MW' },
};

export const acroTesseractStages: AcroTesseractStage[] = [prod];
