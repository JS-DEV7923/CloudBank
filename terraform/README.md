# CloudBank Terraform Scope

This directory contains a reviewable AWS deployment scaffold for the CloudBank API. It is intentionally minimal and should be adjusted before applying in a real AWS account.

Planned resources:

- VPC with public and private subnets.
- ECS Fargate cluster and service for the API container.
- RDS PostgreSQL.
- CloudWatch log group.
- S3 artifact bucket.
- Security groups that allow API-to-database traffic only.

Cost note: RDS and NAT gateways can create ongoing cost. Review variables, use the smallest acceptable sizes, and run `terraform destroy` after practice.
