from rest_framework import viewsets
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from .models import SavingsGoal
from .serializers import SavingsGoalSerializer

class SavingsGoalViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    serializer_class = SavingsGoalSerializer

    def get_queryset(self):
        return SavingsGoal.objects.filter(owner=self.request.user)

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)

    @action(detail=True, methods=['post'], url_path='contribute')
    def contribute(self, request, pk=None):
        goal = self.get_object()
        amount = request.data.get('amount')
        if not amount:
            return Response({'error': 'amount is required'}, status=400)
        goal.current_amount += float(amount)
        goal.save()
        return Response(SavingsGoalSerializer(goal).data)
