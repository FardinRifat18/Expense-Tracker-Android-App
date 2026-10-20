from rest_framework import serializers
from .models import Transaction

class TransactionSerializer(serializers.ModelSerializer):
    category_name = serializers.CharField(source='category.name', read_only=True)
    category_icon = serializers.CharField(source='category.icon', read_only=True)
    category_color = serializers.CharField(source='category.color', read_only=True)

    class Meta:
        model = Transaction
        fields = (
            'id', 'title', 'amount', 'type', 'category', 'category_name',
            'category_icon', 'category_color', 'payment_method',
            'transaction_date', 'description', 'currency', 'created_at'
        )
        read_only_fields = ('id', 'created_at')

    def validate_amount(self, value):
        if value <= 0:
            raise serializers.ValidationError("Transaction amount must be strictly greater than 0.")
        return value
