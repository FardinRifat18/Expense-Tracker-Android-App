from django.db import models
from django.contrib.auth.models import User
from categories.models import Category

class Transaction(models.Model):
    TYPE_CHOICES = (
        ('EXPENSE', 'Expense'),
        ('INCOME', 'Income'),
        ('TRANSFER', 'Transfer'),
    )

    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='transactions')
    category = models.ForeignKey(Category, on_delete=models.PROTECT, related_name='transactions')
    type = models.CharField(max_length=10, choices=TYPE_CHOICES, default='EXPENSE')
    amount = models.DecimalField(max_digits=14, decimal_places=2)
    currency = models.CharField(max_length=10, default='BDT')
    title = models.CharField(max_length=200)
    description = models.TextField(blank=True, default='')
    payment_method = models.CharField(max_length=50, default='bKash')
    transaction_date = models.DateTimeField()
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    class Meta:
        ordering = ['-transaction_date']

    def __str__(self):
        return f"{self.title} - {self.currency} {self.amount}"
