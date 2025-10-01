const mongoose = require('mongoose');

const activityLogSchema = new mongoose.Schema({
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  action: {
    type: String,
    required: true,
    enum: [
      'created_bug',
      'updated_bug',
      'status_changed',
      'assigned_bug',
      'commented',
      'uploaded_attachment',
      'resolved_bug',
      'closed_bug',
      'reopened_bug'
    ]
  },
  targetId: {
    type: mongoose.Schema.Types.ObjectId,
    required: true
  },
  targetType: {
    type: String,
    required: true,
    enum: ['BugReport', 'Comment']
  },
  details: {
    oldValue: mongoose.Schema.Types.Mixed,
    newValue: mongoose.Schema.Types.Mixed,
    metadata: mongoose.Schema.Types.Mixed
  },
  description: {
    type: String,
    required: true,
    trim: true
  }
}, {
  timestamps: true
});

// Indexes for better query performance
activityLogSchema.index({ userId: 1, createdAt: -1 });
activityLogSchema.index({ targetId: 1, targetType: 1 });
activityLogSchema.index({ action: 1 });
activityLogSchema.index({ createdAt: -1 });

// Static method to log activity
activityLogSchema.statics.logActivity = async function(userId, action, targetId, targetType, details = {}, description) {
  try {
    const activity = new this({
      userId,
      action,
      targetId,
      targetType,
      details,
      description
    });

    await activity.save();
    return activity;
  } catch (error) {
    console.error('Error logging activity:', error);
    return null;
  }
};

// Static method to get recent activities for dashboard
activityLogSchema.statics.getRecentActivities = async function(userId, limit = 10) {
  return this.find({ userId })
    .populate('userId', 'name email')
    .sort({ createdAt: -1 })
    .limit(limit)
    .lean();
};

// Static method to get activity feed (all users or specific user's activities)
activityLogSchema.statics.getActivityFeed = async function(options = {}) {
  const { userId, limit = 20, skip = 0, actions = [] } = options;

  const query = {};
  if (userId) query.userId = userId;
  if (actions.length > 0) query.action = { $in: actions };

  return this.find(query)
    .populate('userId', 'name email role')
    .populate('targetId')
    .sort({ createdAt: -1 })
    .limit(limit)
    .skip(skip)
    .lean();
};

module.exports = mongoose.model('ActivityLog', activityLogSchema);