// MongoDB initialization script
db = db.getSiblingDB('deal_pipeline_db');

// Create application user
db.createUser({
  user: 'app_user',
  pwd: 'app_password',
  roles: [
    {
      role: 'readWrite',
      db: 'deal_pipeline_db'
    }
  ]
});

// Create initial collections and indexes if needed
db.createCollection('deals');
db.createCollection('users');
db.createCollection('stages');

// Create indexes for better performance
db.deals.createIndex({ "createdAt": 1 });
db.deals.createIndex({ "status": 1 });
db.users.createIndex({ "email": 1 }, { unique: true });
db.stages.createIndex({ "pipelineId": 1 });

print('MongoDB initialized successfully');
