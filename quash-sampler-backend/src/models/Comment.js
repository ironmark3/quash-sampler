const mongoose = require('mongoose');

const commentSchema = new mongoose.Schema({
  bugId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'BugReport',
    required: true
  },
  userId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  comment: {
    type: String,
    required: true,
    trim: true,
    maxlength: 1000
  },
  mentions: [{
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    username: String
  }],
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
  isEdited: {
    type: Boolean,
    default: false
  },
  editedAt: {
    type: Date
  },
  reactions: [{
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'User'
    },
    type: {
      type: String,
      enum: ['like', 'useful', 'resolved']
    }
  }]
}, {
  timestamps: true
});

// Indexes for better query performance
commentSchema.index({ bugId: 1, createdAt: -1 });
commentSchema.index({ userId: 1 });

// Virtual for reaction counts
commentSchema.virtual('reactionCounts').get(function() {
  const counts = { like: 0, useful: 0, resolved: 0 };

  this.reactions.forEach(reaction => {
    if (counts.hasOwnProperty(reaction.type)) {
      counts[reaction.type]++;
    }
  });

  return counts;
});

// Method to add or remove reaction
commentSchema.methods.toggleReaction = function(userId, reactionType) {
  const existingReactionIndex = this.reactions.findIndex(
    reaction => reaction.userId.toString() === userId.toString() && reaction.type === reactionType
  );

  if (existingReactionIndex > -1) {
    // Remove existing reaction
    this.reactions.splice(existingReactionIndex, 1);
    return { action: 'removed', type: reactionType };
  } else {
    // Add new reaction
    this.reactions.push({ userId, type: reactionType });
    return { action: 'added', type: reactionType };
  }
};

// Pre-save middleware to handle edits
commentSchema.pre('save', function(next) {
  if (this.isModified('comment') && !this.isNew) {
    this.isEdited = true;
    this.editedAt = new Date();
  }
  next();
});

module.exports = mongoose.model('Comment', commentSchema);