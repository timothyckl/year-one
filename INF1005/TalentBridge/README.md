# TalentBridge

A job-board platform connecting job seekers, employers, and administrators, built with the LAMP stack.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | PHP 8+ (no framework) |
| Database | MySQL 8.0 |
| Web server | Apache 2.4 with `.htaccess` |
| Frontend | Bootstrap 5.3.3, Vanilla ES6+ |
| Testing | PHPUnit 11 |

## Roles

| Role | Key capabilities |
|---|---|
| Seeker | Browse and apply for jobs, manage profile and CV, save listings |
| Employer | Post and manage job listings, review applicants |
| Admin | Manage users and listings, view contact messages, audit log |

## Features

- Role-based authentication with session fixation protection
- Email verification and password reset via tokenised links
- Brute-force protection — per-account progressive lockout and site-wide IP throttling
- CSRF protection on all forms
- Secure CV upload — MIME, extension, and magic byte validation; stored outside web root
- Security audit log (admin-only)
- HTTP security headers — CSP, HSTS, `X-Frame-Options`, `X-Content-Type-Options`
- Real-time search and filtering across listings and admin panels

## Project Structure

```
├── index.php / login.php / register.php / jobs.php ...   # public pages
├── admin/          # admin panel
├── employer/       # employer portal
├── seeker/         # seeker portal
├── includes/       # shared PHP (db, auth, csrf, helpers, mailer, tokens)
├── assets/         # CSS and JS
└── sql/schema.sql  # full database schema
```
