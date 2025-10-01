const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    trim: true,
    maxlength: 100
  },
  email: {
    type: String,
    required: false,
    unique: true,
    sparse: true,
    lowercase: true,
    trim: true
  },
  phone: {
    type: String,
    required: false,
    unique: true,
    sparse: true,
    trim: true
  },
  address: {
    type: String,
    trim: true,
    maxlength: 500
  },
  dateOfBirth: {
    type: Date
  },
  role: {
    type: String,
    enum: ['Reporter', 'Developer', 'QA'],
    default: 'Reporter'
  },
  isProfileComplete: {
    type: Boolean,
    default: false
  }
}, {
  timestamps: true
});

// Index for better query performance
userSchema.index({ email: 1 });
userSchema.index({ phone: 1 });

// Virtual for user's full profile completion status
userSchema.virtual('profileCompletionPercentage').get(function() {
  let completed = 0;

  // Core required fields (worth more points)
  const coreFields = ['name', 'role'];
  const coreWeight = 3; // Each core field is worth 30 points

  coreFields.forEach(field => {
    if (this[field]) completed += coreWeight;
  });

  // Contact info (required but only count if present)
  if (this.email || this.phone) completed += 2; // Worth 20 points

  // Optional enhancement fields (worth less)
  const optionalFields = ['address', 'dateOfBirth'];
  const optionalWeight = 1; // Each optional field is worth 10 points

  optionalFields.forEach(field => {
    if (this[field]) completed += optionalWeight;
  });

  // Total possible: (2 core * 3) + (1 contact * 2) + (2 optional * 1) = 10 points
  const totalPossible = (coreFields.length * coreWeight) + 2 + (optionalFields.length * optionalWeight);

  return Math.round((completed / totalPossible) * 100);
});

// Method to check if profile is complete
userSchema.methods.checkProfileCompletion = function() {
  // Only require core fields that are collected during onboarding
  const requiredFields = ['name', 'role'];
  // Additional fields that make profile more complete
  const optionalFields = ['address', 'dateOfBirth'];

  const hasRequiredFields = requiredFields.every(field => this[field]);
  // At least email or phone must be present (enforced in pre-save)
  const hasContactInfo = this.email || this.phone;

  // Profile is complete if has required fields and contact info
  const isComplete = hasRequiredFields && hasContactInfo;

  if (isComplete !== this.isProfileComplete) {
    this.isProfileComplete = isComplete;
  }

  return isComplete;
};

// Pre-save middleware to update profile completion status and validate email/phone
userSchema.pre('save', function(next) {
  // Ensure at least email or phone is provided
  if (!this.email && !this.phone) {
    const error = new Error('Either email or phone must be provided');
    error.name = 'ValidationError';
    return next(error);
  }

  this.checkProfileCompletion();
  next();
});

module.exports = mongoose.model('User', userSchema);