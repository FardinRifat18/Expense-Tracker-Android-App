from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/v1/auth/', include('accounts.urls')),
    path('api/v1/transactions/', include('transactions.urls')),
    path('api/v1/categories/', include('categories.urls')),
    path('api/v1/budgets/', include('budgets.urls')),
    path('api/v1/savings/', include('savings.urls')),
    path('api/v1/analytics/', include('analytics.urls')),
]
