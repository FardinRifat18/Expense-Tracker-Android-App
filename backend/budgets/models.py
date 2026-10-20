from django.db import models
from django.contrib.auth.models import User
from categories.models import Category

class Budget(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='budgets')
    category = models.ForeignKey(Category, on_delete=models.CASCADE, null=True, blank=True, related_name='budgets')
    amount = models.DecimalField(max_digits=14, decimal_places=2)
    period = models.CharField(max_length=7) # e.g. "2026-10"
    currency = models.CharField(max_length=10, default='BDT')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        unique_together = ('owner', 'category', 'period')

    def __str__(self):
        cat_name = self.category.name if self.category else "Overall"
        return f"{cat_name} Budget - {self.currency} {self.amount} ({self.period})"
