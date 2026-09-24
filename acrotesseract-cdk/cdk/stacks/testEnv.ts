import * as cdk from 'aws-cdk-lib';

/** Fixed account/region so stacks with cross-stack references synthesize in tests. */
export const testEnv: cdk.Environment = { account: '111111111111', region: 'us-west-2' };
