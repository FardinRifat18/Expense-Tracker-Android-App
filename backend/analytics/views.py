from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.permissions import IsAuthenticated
from django.db.models import Sum
from transactions.models import Transaction

class AnalyticsSummaryView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        opening_balance = float(user.profile.opening_balance) if hasattr(user, 'profile') else 50000.0

        all_income = Transaction.objects.filter(owner=user, type='INCOME').aggregate(Sum('amount'))['amount__sum'] or 0
        all_expenses = Transaction.objects.filter(owner=user, type='EXPENSE').aggregate(Sum('amount'))['amount__sum'] or 0

        total_balance = opening_balance + float(all_income) - float(all_expenses)
        net_savings = float(all_income) - float(all_expenses)

        return Response({
            'total_balance': str(total_balance),
            'total_income': str(all_income),
            'total_expenses': str(all_expenses),
            'net_savings': str(net_savings),
            'currency': 'BDT'
        })

class CategoryBreakdownView(APIView):
    permission_classes = [IsAuthenticated]

    def get(self, request):
        user = request.user
        period = request.query_params.get('period') # e.g. "2026-10"
        qs = Transaction.objects.filter(owner=user, type='EXPENSE')
        if period:
            qs = qs.filter(transaction_date__startswith=period)

        categories = qs.values('category__name', 'category__icon', 'category__color').annotate(
            total_spent=Sum('amount')
        ).order_by('-total_spent')

        total_expense = qs.aggregate(Sum('amount'))['amount__sum'] or 1
        result = []
        for c in categories:
            spent = float(c['total_spent'] or 0)
            percent = (spent / float(total_expense)) * 100
            result.append({
                'category_name': c['category__name'],
                'icon': c['category__icon'],
                'color': c['category__color'],
                'total_spent': str(spent),
                'percentage': round(percent, 1)
            })
        return Response(result)
