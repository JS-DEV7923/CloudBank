terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_vpc" "cloudbank" {
  cidr_block           = "10.40.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "cloudbank-vpc"
  }
}

resource "aws_cloudwatch_log_group" "api" {
  name              = "/ecs/cloudbank-api"
  retention_in_days = 14
}

resource "aws_s3_bucket" "artifacts" {
  bucket_prefix = "cloudbank-artifacts-"
}

resource "aws_s3_bucket_public_access_block" "artifacts" {
  bucket                  = aws_s3_bucket.artifacts.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_ecs_cluster" "cloudbank" {
  name = "cloudbank-cluster"
}

resource "aws_security_group" "api" {
  name        = "cloudbank-api-sg"
  description = "CloudBank API security group"
  vpc_id      = aws_vpc.cloudbank.id
}

resource "aws_security_group" "db" {
  name        = "cloudbank-db-sg"
  description = "CloudBank RDS security group"
  vpc_id      = aws_vpc.cloudbank.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.api.id]
  }
}

# Subnets, routing, ECS task definitions, and RDS subnet groups are intentionally
# left for environment-specific completion so reviewers can see the intended
# resource boundaries without accidentally applying costly defaults.
