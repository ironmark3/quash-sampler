const validateBugReport = (req, res, next) => {
  const { title, description, priority, category, environment } = req.body;
  const errors = [];

  // Required fields validation
  if (!title || title.trim().length === 0) {
    errors.push('Title is required');
  } else if (title.length > 200) {
    errors.push('Title must be less than 200 characters');
  }

  if (!description || description.trim().length === 0) {
    errors.push('Description is required');
  } else if (description.length > 2000) {
    errors.push('Description must be less than 2000 characters');
  }

  if (!environment || environment.trim().length === 0) {
    errors.push('Environment is required');
  } else if (environment.length > 500) {
    errors.push('Environment must be less than 500 characters');
  }

  // Priority validation
  const validPriorities = ['Low', 'Medium', 'High', 'Critical'];
  if (priority && !validPriorities.includes(priority)) {
    errors.push('Priority must be one of: Low, Medium, High, Critical');
  }

  // Category validation
  const validCategories = ['UI/UX', 'Functionality', 'Performance', 'Security', 'Compatibility', 'Data', 'Other'];
  if (category && !validCategories.includes(category)) {
    errors.push('Category must be one of: UI/UX, Functionality, Performance, Security, Compatibility, Data, Other');
  }

  // Steps to reproduce validation (optional but if provided should be meaningful)
  if (req.body.stepsToReproduce && req.body.stepsToReproduce.length > 1000) {
    errors.push('Steps to reproduce must be less than 1000 characters');
  }

  // Expected behavior validation (optional)
  if (req.body.expectedBehavior && req.body.expectedBehavior.length > 500) {
    errors.push('Expected behavior must be less than 500 characters');
  }

  // Actual behavior validation (optional)
  if (req.body.actualBehavior && req.body.actualBehavior.length > 500) {
    errors.push('Actual behavior must be less than 500 characters');
  }


  if (errors.length > 0) {
    return res.status(400).json({
      success: false,
      message: 'Validation failed',
      errors
    });
  }

  next();
};

const validateComment = (req, res, next) => {
  const { content } = req.body;
  const errors = [];

  // Required fields validation
  if (!content || content.trim().length === 0) {
    errors.push('Comment content is required');
  } else if (content.length > 1000) {
    errors.push('Comment must be less than 1000 characters');
  }

  // Comment type validation (optional)
  const validTypes = ['comment', 'status_update', 'solution'];
  if (req.body.type && !validTypes.includes(req.body.type)) {
    errors.push('Comment type must be one of: comment, status_update, solution');
  }

  if (errors.length > 0) {
    return res.status(400).json({
      success: false,
      message: 'Validation failed',
      errors
    });
  }

  next();
};

module.exports = {
  validateBugReport,
  validateComment
};