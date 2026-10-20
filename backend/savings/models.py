from django.db import models
from django.contrib.auth.models import User

class SavingsGoal(models.Model):
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='savings_goals')
    title = models.CharField(max_length=200)
    target_amount = models.DecimalField(max_digits=14, decimal_places=2)
    current_amount = models.DecimalField(max_digits=14, decimal_places=2, default=0.00)
    currency = models.CharField(max_length=10, default='BDT')
    target_date = models.DateField()
    status = models.CharField(max_length=20, default='active')
    color = models.CharField(max_length=20, default='#00A86B')
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.title} - {self.currency} {self.current_amount}/{self.target_amount}"
