#!/bin/bash
# ─────────────────────────────────────────────────────────────────
# push.sh — Initialize git and push to your GitHub repository
#
# Usage:
#   chmod +x push.sh
#   ./push.sh
#
# You will be prompted for your GitHub Personal Access Token.
# Create one at: https://github.com/settings/tokens (needs 'repo' scope)
# ─────────────────────────────────────────────────────────────────

set -e

REPO_URL="https://github.com/meranaamkhann/finance-dashboard.git"
BRANCH="main"

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║     Finance Dashboard — GitHub Push Script           ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

# Check if already a git repo
if [ ! -d ".git" ]; then
  echo "→ Initializing git repository..."
  git init
  git branch -M $BRANCH
else
  echo "→ Git repository already initialized."
fi

# Stage everything
echo "→ Staging all files..."
git add .

# Commit
echo "→ Creating initial commit..."
git commit -m "feat: Finance Dashboard API v1.0.0

- JWT authentication with role-based access control (VIEWER/ANALYST/ADMIN)
- Financial records CRUD with dynamic filtering, tags, soft-delete
- Budget management with real-time spend tracking and alert status
- Budget alert engine — warning (80%) and critical (100%) notifications
- Recurring transaction scheduler — auto-posts on DAILY/WEEKLY/MONTHLY/QUARTERLY/YEARLY rules
- Financial health score — composite 0-100 from 5 signals with grade and insights
- Immutable audit trail — every mutation logged with before/after state and IP
- CSV export with same filter params as list endpoint
- Per-IP rate limiting via Bucket4j (100 req/60s)
- Response caching with automatic eviction on writes
- Swagger UI at /swagger-ui.html
- 6 integration test classes + 3 unit test classes" 2>/dev/null || echo "→ Nothing new to commit."

# Add remote (won't fail if already exists)
git remote remove origin 2>/dev/null || true
git remote add origin $REPO_URL

echo ""
echo "→ Pushing to $REPO_URL ..."
echo "   When prompted for a password, enter your GitHub Personal Access Token."
echo "   (Get one at: https://github.com/settings/tokens — needs 'repo' scope)"
echo ""

git push -u origin $BRANCH

echo ""
echo "✅  Done! Visit: https://github.com/meranaamkhann/finance-dashboard"
echo ""
