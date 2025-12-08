import json

class Customer:
    def __init__(self, customer_id, name, role):
        self.customer_id = customer_id
        self.name = name
        self.role = role

    def to_dict(self):
        return {
            "customer_id": self.customer_id,
            "name": self.name,
            "role": self.role
        }
