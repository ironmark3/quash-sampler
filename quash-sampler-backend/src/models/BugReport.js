const mongoose = require('mongoose');

const bugReportSchema = new mongoose.Schema({
  bugId: {
    type: String,
    unique: true,
    required: false
  },
  title: {
    type: String,
    required: true,
    trim: true,
    maxlength: 200
  },
  description: {
    type: String,
    required: true,
    trim: true,
    maxlength: 2000
  },
  stepsToReproduce: {
    type: String,
    required: false,
    trim: true,
    maxlength: 1000
  },
  expectedBehavior: {
    type: String,
    trim: true,
    maxlength: 500
  },
  actualBehavior: {
    type: String,
    trim: true,
    maxlength: 500
  },
  severity: {
    type: String,
    enum: ['Low', 'Medium', 'High', 'Critical'],
    required: true,
    default: 'Medium'
  },
  category: {
    type: String,
    enum: ['UI/UX', 'Functionality', 'Performance', 'Security', 'Compatibility', 'Data', 'Other'],
    required: true,
    default: 'Other'
  },
  status: {
    type: String,
    enum: ['Open', 'In Progress', 'Testing', 'Resolved', 'Closed', 'Reopened'],
    default: 'Open'
  },
  reporter: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  assignedTo: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  attachments: [{
    filename: String,
    originalName: String,
    mimetype: String,
    size: Number,
    path: String,
    uploadedAt: {
      type: Date,
      default: Date.now
    }
  }],
  environment: {
    type: String,
    required: true,
    trim: true,
    maxlength: 500
  },
  priority: {
    type: String,
    enum: ['Low', 'Medium', 'High', 'Critical'],
    required: true,
    default: 'Medium'
  },
  tags: [{
    type: String,
    trim: true
  }],
  resolution: {
    type: String,
    trim: true,
    maxlength: 1000
  },
  resolvedAt: {
    type: Date
  },
  resolvedBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  }
}, {
  timestamps: true
});

// Indexes for better query performance
bugReportSchema.index({ bugId: 1 });
bugReportSchema.index({ reporter: 1 });
bugReportSchema.index({ assignedTo: 1 });
bugReportSchema.index({ status: 1 });
bugReportSchema.index({ severity: 1 });
bugReportSchema.index({ category: 1 });
bugReportSchema.index({ createdAt: -1 });

// Compound indexes for common queries
bugReportSchema.index({ status: 1, severity: 1 });
bugReportSchema.index({ reporter: 1, status: 1 });
bugReportSchema.index({ assignedTo: 1, status: 1 });

// Virtual for calculating days since creation
bugReportSchema.virtual('daysSinceCreated').get(function() {
  const now = new Date();
  const diffTime = Math.abs(now - this.createdAt);
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
});

// Virtual for checking if bug is overdue (more than 7 days in Open status)
bugReportSchema.virtual('isOverdue').get(function() {
  return this.status === 'Open' && this.daysSinceCreated > 7;
});

// Static method to generate next bug ID
bugReportSchema.statics.generateBugId = async function() {
  const lastBug = await this.findOne().sort({ createdAt: -1 });

  if (!lastBug) {
    return 'BUG-001';
  }

  const lastNumber = parseInt(lastBug.bugId.split('-')[1]);
  const nextNumber = lastNumber + 1;

  return `BUG-${nextNumber.toString().padStart(3, '0')}`;
};

// Pre-save middleware to generate bug ID
bugReportSchema.pre('save', async function(next) {
  if (this.isNew && !this.bugId) {
    this.bugId = await this.constructor.generateBugId();
  }

  // Update resolved timestamp when status changes to resolved
  if (this.isModified('status')) {
    if (this.status === 'Resolved' && !this.resolvedAt) {
      this.resolvedAt = new Date();
    } else if (this.status !== 'Resolved') {
      this.resolvedAt = undefined;
    }
  }

  next();
});

module.exports = mongoose.model('BugReport', bugReportSchema);