import csv
from django.http import HttpResponse
from rest_framework import viewsets, filters
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated
from django_filters.rest_framework import DjangoFilterBackend
from .models import Transaction
from .serializers import TransactionSerializer

class TransactionViewSet(viewsets.ModelViewSet):
    permission_classes = [IsAuthenticated]
    serializer_class = TransactionSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['type', 'category', 'payment_method']
    search_fields = ['title', 'description', 'category__name']
    ordering_fields = ['transaction_date', 'amount']

    def get_queryset(self):
        return Transaction.objects.filter(owner=self.request.user)

    def perform_create(self, serializer):
        serializer.save(owner=self.request.user)

    @action(detail=False, methods=['get'], url_path='export-csv')
    def export_csv(self, request):
        transactions = self.get_queryset()
        response = HttpResponse(content_type='text/csv')
        response['Content-Disposition'] = 'attachment; filename="ExpenseFlow_Transactions.csv"'

        writer = csv.writer(response)
        writer.writerow(['ID', 'Date', 'Title', 'Type', 'Category', 'Amount', 'Currency', 'Payment Method', 'Description'])
        for tx in transactions:
            writer.writerow([
                tx.id,
                tx.transaction_date.strftime('%Y-%m-%d %H:%M'),
                tx.title,
                tx.type,
                tx.category.name,
                tx.amount,
                tx.currency,
                tx.payment_method,
                tx.description
            ])
        return response
