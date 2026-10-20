from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated
from .models import Budget
from .serializers import BudgetSerializer

class BudgetViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    serializer_class = BudgetSerializer

    def get_queryset(self):
        qs = Budget.objects.filter(owner=self.request.user)
        period = self.request.query_params.get('period')
        if period:
            qs = qs.filter(period=period)
        return qs

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)
