# Contributing to SmartLoan AI+

Thank you for your interest in contributing to SmartLoan AI+! This document provides guidelines and instructions for contributing.

## 📋 Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help others learn and grow

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- Python 3.8+
- Android Studio (for mobile development)
- Git

### Setup Development Environment

```bash
# Clone the repository
git clone https://github.com/yourusername/smartloan-ai-mobile.git
cd smartloan-ai-mobile

# Backend setup
cd backend
npm install
cp .env.template .env
# Edit .env with your configuration
npm run dev

# ML Service setup
cd ../ml-service
python -m venv venv
source venv/bin/activate  # or: venv\Scripts\activate on Windows
pip install -r requirements.txt
python main.py

# Android app
# Open android/ folder in Android Studio
```

## 📝 Commit Guidelines

### Branch Naming
- `feature/feature-name` - New features
- `bugfix/bug-name` - Bug fixes
- `hotfix/issue-name` - Urgent fixes
- `docs/doc-name` - Documentation
- `refactor/change-name` - Code refactoring

### Commit Messages

```
type(scope): subject (50 chars max)

Detailed explanation of what changed and why.
References: #123
```

**Types**:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `style` - Formatting, missing semicolons, etc.
- `refactor` - Code refactoring
- `test` - Adding or updating tests
- `chore` - Build, dependencies, etc.

**Examples**:
```
feat(auth): add Firebase authentication support
fix(api): resolve CORS issue for mobile clients
docs(readme): update deployment instructions
```

## 🧪 Testing

Before submitting, ensure:

```bash
# Backend
cd backend
npm test              # Run tests
npm run lint          # Check code quality

# ML Service
cd ../ml-service
source venv/bin/activate
pytest tests/         # Run tests
```

## 📋 Pull Request Process

1. **Create a branch** from `develop`
   ```bash
   git checkout -b feature/your-feature
   ```

2. **Make your changes** and commit regularly
   ```bash
   git add .
   git commit -m "feat(scope): description"
   ```

3. **Keep your branch updated**
   ```bash
   git fetch origin
   git rebase origin/develop
   ```

4. **Push your branch**
   ```bash
   git push origin feature/your-feature
   ```

5. **Create a Pull Request** on GitHub with:
   - Clear description of changes
   - References to related issues
   - Screenshots (for UI changes)
   - Test results

6. **Respond to feedback** and make requested changes

## 🎯 Development Guidelines

### Code Style

**JavaScript/Node.js**:
- Use ES6+ syntax
- Follow ESLint rules
- Use meaningful variable names
- Add JSDoc comments for functions

**Python**:
- Follow PEP 8 style guide
- Use type hints
- Add docstrings to functions
- Use meaningful variable names

**Kotlin/Android**:
- Follow Android coding standards
- Use ktlint for formatting
- Follow Material Design guidelines
- Add comments for complex logic

### Naming Conventions

| Type | Convention | Example |
|---|---|---|
| Variables | camelCase | `userName`, `isActive` |
| Constants | UPPER_SNAKE_CASE | `MAX_FILE_SIZE`, `API_TIMEOUT` |
| Classes | PascalCase | `UserService`, `AuthController` |
| Functions | camelCase | `getUserData()`, `validateEmail()` |
| Files | kebab-case | `user-service.js`, `auth-routes.py` |

### Documentation

- Add comments for complex logic
- Update README.md for significant changes
- Document breaking changes
- Add examples for new features

## 🐛 Reporting Bugs

Include:
- Clear title and description
- Steps to reproduce
- Expected vs actual behavior
- Environment details (OS, versions)
- Screenshots or error logs
- Possible solutions (if any)

## 🎨 Feature Requests

Include:
- Clear description of the feature
- Use cases and benefits
- Proposed implementation (if any)
- Mockups or examples
- Potential challenges

## 📦 Release Process

Releases follow semantic versioning: `MAJOR.MINOR.PATCH`

- `MAJOR` - Breaking changes
- `MINOR` - New features (backward compatible)
- `PATCH` - Bug fixes

## 🤝 Community

- Join our [Discord](https://discord.gg/smartloan)
- Check [GitHub Discussions](https://github.com/smartloan/smartloan-ai/discussions)
- Read our [Blog](https://smartloan.ai/blog)

## ✅ Checklist

Before submitting your PR:

- [ ] Code follows project style guidelines
- [ ] Tests written and passing
- [ ] Documentation updated
- [ ] No console errors or warnings
- [ ] No hardcoded secrets or credentials
- [ ] Commit messages are clear and descriptive
- [ ] Branch is up to date with develop
- [ ] Changes don't break existing functionality

## 📚 Additional Resources

- [Architecture Guide](ARCHITECTURE.md)
- [Deployment Guide](DEPLOYMENT.md)
- [API Reference](README.md#api-reference)
- [Development Setup](README.md#quick-start-local-development)

---

**Thank you for contributing to SmartLoan AI+!** 🚀

Questions? Open an issue or reach out to the maintainers.
