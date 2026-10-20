# ExpenseFlow Setup & Deployment Guide

## 1. Running the Android Client
1. Open the project in Android Studio or compile directly using Gradle.
2. The Android client is designed **offline-first** with Room local SQLite database. It initializes immediately with realistic Bangladeshi Taka (৳) sample transactions, budgets, categories, and savings goals so you can test all features with zero server setup required.
3. To build the debug APK:
   ```bash
   gradle assembleDebug
   ```

## 2. Running the Django Backend
1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python3 -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run migrations:
   ```bash
   python manage.py makemigrations accounts categories transactions budgets savings analytics
   python manage.py migrate
   ```
5. Create an administrative superuser:
   ```bash
   python manage.py createsuperuser
   ```
6. Start the development server:
   ```bash
   python manage.py runserver 0.0.0.0:8000
   ```

## 3. Connecting Android App to Django Server
- **Android Emulator**: In the app's Profile screen -> Django REST Backend URL, set `http://10.0.2.2:8000/api/v1/`.
- **Physical Android Phone**: Find your machine's LAN IP (e.g., `192.168.1.100`), ensure both phone and computer are on the same Wi-Fi, and set the URL to `http://192.168.1.100:8000/api/v1/`.
- Tap **Test** in the Profile screen to verify real-time connectivity.
