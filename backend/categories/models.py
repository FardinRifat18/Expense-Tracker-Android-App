from django.db import models
from django.contrib.auth.models import User

class Category(models.Model):
    TYPE_CHOICES = (
        ('EXPENSE', 'Expense'),
        ('INCOME', 'Income'),
    )

    owner = models.ForeignKey(User, on_delete=models.CASCADE, null=True, blank=True, related_name='categories')
    name = models.CharField(max_length=100)
    type = models.CharField(max_length=10, choices=TYPE_CHOICES, default='EXPENSE')
    icon = models.CharField(max_length=50, default='category')
    color = models.CharField(max_length=20, default='#00A86B')
    is_default = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name_plural = 'Categories'

    def __str__(self):
        return f"{self.name} ({self.type})"
