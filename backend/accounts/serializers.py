from rest_framework import serializers
from django.contrib.auth.models import User
from .models import UserProfile

class UserSerializer(serializers.ModelSerializer):
    preferred_currency = serializers.CharField(source='profile.preferred_currency', read_only=True)
    opening_balance = serializers.DecimalField(source='profile.opening_balance', max_digits=14, decimal_places=2, read_only=True)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'first_name', 'preferred_currency', 'opening_balance')

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)
    preferred_currency = serializers.CharField(default='BDT', write_only=True)

    class Meta:
        model = User
        fields = ('username', 'email', 'password', 'first_name', 'preferred_currency')

    def create(self, validated_data):
        currency = validated_data.pop('preferred_currency', 'BDT')
        user = User.objects.create_user(
            username=validated_data.get('email') or validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password'],
            first_name=validated_data.get('first_name', '')
        )
        UserProfile.objects.create(user=user, preferred_currency=currency)
        return user
