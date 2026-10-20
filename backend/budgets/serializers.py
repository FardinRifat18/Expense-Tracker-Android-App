from rest_framework import serializers
from django.db.models import Sum
from .models import Budget
from transactions.models import Transaction

class BudgetSerializer(serializers.ModelSerializer):
    category_name = serializers.CharField(source='category.name', read_only=True, default='Overall Monthly')
    spent_amount = serializers.SerializerMethodField()
    remaining_amount = serializers.SerializerMethodField()
    percentage_used = serializers.SerializerMethodField()

    class Meta:
        model = Budget
        fields = (
            'id', 'category', 'category_name', 'amount', 'period',
            'currency', 'spent_amount', 'remaining_amount', 'percentage_used'
        )
        read_only_fields = ('id',)

    def get_spent_amount(self, obj):
        qs = Transaction.objects.filter(
            owner=obj.owner,
            type='EXPENSE',
            transaction_date__startswith=obj.period
        )
        if obj.category:
            qs = qs.filter(category=obj.category)
        total = qs.aggregate(Sum('amount'))['amount__sum'] or 0
        return str(total)

    def get_remaining_amount(self, obj):
        spent = float(self.get_spent_amount(obj))
        rem = max(0.0, float(obj.amount) - spent)
        return str(rem)

    def get_percentage_used(self, obj):
        limit = float(obj.amount)
        if limit <= 0:
            return 0.0
        spent = float(self.get_spent_amount(obj))
        return round((spent / limit) * 100, 1)
