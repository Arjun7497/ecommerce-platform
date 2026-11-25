variable "aws_region" {
  description = "AWS region to deploy resources"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Project name used as prefix for all resources"
  type        = string
  default     = "ecommerce"
}

variable "db_username" {
  description = "RDS master username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
}

variable "enable_frontend" {
  description = "Deploy the frontend on ECS (playground allows only 3 Fargate tasks per cluster)"
  type        = bool
  default     = false
}

variable "execution_role_arn" {
  description = "Existing ECS task execution role ARN; leave empty to let Terraform create one"
  type        = string
  default     = ""
}
