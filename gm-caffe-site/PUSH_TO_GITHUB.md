# Push to GitHub - Manual Instructions

Since there was a network issue, follow these steps to push your code to GitHub:

## Option 1: Using GitHub CLI (when internet is working)

Run these commands in your terminal:

```
bash
cd gm-caffe-site

# Create the repository on GitHub
gh repo create gm-caffe-vijayapura --public --source=. --description "GM Cafe Vijayapura - Premium Coffee Shop Website with Admin Panel" --push

# Or if above fails, use:
gh repo create gm-caffe-vijayapura --public
git remote add origin https://github.com/YOUR_USERNAME/gm-caffe-vijayapura.git
git push -u origin master
```

## Option 2: Using Git directly

```
bash
# First, create an empty repository on GitHub.com manually:
# 1. Go to https://github.com/new
# 2. Repository name: gm-caffe-vijayapura
# 3. Don't initialize with README
# 4. Click "Create repository"

# Then run these commands:
cd gm-caffe-site
git remote add origin https://github.com/YOUR_USERNAME/gm-caffe-vijayapura.git
git push -u origin master
```

Replace `YOUR_USERNAME` with your actual GitHub username.

## After pushing to GitHub, deploy to Render.com (Free)

1. Go to https://render.com and sign up
2. Connect your GitHub account
3. Click "New" → "Web Service"
4. Select your repository: `gm-caffe-vijayapura`
5. Configure:
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/gm-caffe-site-1.0.0.jar`
6. Click "Create Web Service"

Your site will be live at a URL like `https://gm-caffe-vijayapura.onrender.com`

## Already done:
- ✅ Added SEO meta tags to all pages
- ✅ Created .gitignore file
- ✅ Created production configuration
- ✅ Created deployment guide
- ✅ Committed all changes locally
