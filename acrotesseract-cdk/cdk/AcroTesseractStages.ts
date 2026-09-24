export interface AcroTesseractStage {
  /** Used in stack names (`acrotesseract-<purpose>-stack-<stageName>`), the table name and SSM paths. */
  stageName: string;
  accountId: string;
  region: string;
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
};

export const acroTesseractStages: AcroTesseractStage[] = [prod];
