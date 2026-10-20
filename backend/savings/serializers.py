from rest_framework import serializers
from .models import SavingsGoal

class SavingsGoalSerializer(serializers.ModelSerializer):
    progress_percentage = serializers.SerializerMethodField()
    remaining_amount = serializers.SerializerMethodField()

    class Meta:
        model = SavingsGoal
        fields = (
            'id', 'title', 'target_amount', 'current_amount', 'currency',
            'target_date', 'status', 'color', 'progress_percentage', 'remaining_amount'
        )
        read_only_fields = ('id',)

    def get_progress_percentage(self, obj):
        target = float(obj.target_amount)
        if target <= 0:
            return 0.0
        current = float(obj.current_amount)
        return min(100.0, round((current / target) * 100, 1))

    def get_remaining_amount(self, obj):
        rem = max(0.0, float(obj.target_amount) - float(obj.current_amount))
        return str(rem)
