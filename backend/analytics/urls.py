from django.urls import path
from .views import AnalyticsSummaryView, CategoryBreakdownView

urlpatterns = [
    path('summary/', AnalyticsSummaryView.as_view(), name='analytics-summary'),
    path('category-breakdown/', CategoryBreakdownView.as_view(), name='category-breakdown'),
]
