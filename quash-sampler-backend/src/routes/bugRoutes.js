const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const BugService = require('../services/BugService');
const CommentService = require('../services/CommentService');
const { validateBugReport, validateComment } = require('../middleware/validation');

const router = express.Router();

// Configure multer for file uploads (screenshots, attachments)
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = 'uploads/bugs';
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, `${file.fieldname}-${uniqueSuffix}${path.extname(file.originalname)}`);
  }
});

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 10 * 1024 * 1024, // 10MB limit
    files: 5 // Maximum 5 files
  },
  fileFilter: (req, file, cb) => {
    // Allow images, videos, and log files
    const allowedTypes = /jpeg|jpg|png|gif|mp4|mov|avi|txt|log|pdf/;
    const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
    const mimetype = allowedTypes.test(file.mimetype);

    if (mimetype && extname) {
      return cb(null, true);
    } else {
      cb(new Error('Only images, videos, and log files are allowed'));
    }
  }
});

// CREATE: Create new bug report
router.post('/', upload.array('attachments', 5), validateBugReport, async (req, res) => {
  try {
    console.log('📝 Bug Report Request Body:', JSON.stringify(req.body, null, 2));
    console.log('📎 Files:', req.files?.length || 0);

    const bugData = {
      ...req.body,
      reporter: req.body.reporter || req.user?.id, // Use authenticated user ID when auth is implemented
      attachments: req.files ? req.files.map(file => ({
        filename: file.filename,
        originalName: file.originalname,
        path: file.path,
        size: file.size,
        mimetype: file.mimetype
      })) : []
    };

    const bug = await BugService.createBug(bugData);

    // Transform the response to match Android app expectations
    const transformedBug = {
      id: bug._id.toString(),
      bugId: bug.bugId,
      title: bug.title,
      description: bug.description,
      status: bug.status,
      priority: bug.priority,
      category: bug.category,
      reporter: {
        id: bug.reporter._id.toString(),
        name: bug.reporter.name,
        email: bug.reporter.email,
        role: bug.reporter.role
      },
      assignedTo: bug.assignedTo ? {
        id: bug.assignedTo._id.toString(),
        name: bug.assignedTo.name,
        email: bug.assignedTo.email,
        role: bug.assignedTo.role
      } : null,
      stepsToReproduce: bug.stepsToReproduce,
      expectedBehavior: bug.expectedBehavior,
      actualBehavior: bug.actualBehavior,
      environment: bug.environment,
      attachments: bug.attachments.map(att => ({
        filename: att.filename,
        originalName: att.originalName,
        path: att.path,
        size: att.size,
        mimetype: att.mimetype
      })),
      tags: bug.tags,
      reproducibility: bug.reproducibility,
      severity: bug.severity,
      createdAt: bug.createdAt.toISOString(),
      updatedAt: bug.updatedAt.toISOString()
    };

    res.status(201).json({
      success: true,
      message: 'Bug report created successfully',
      bug: transformedBug
    });
  } catch (error) {
    // Clean up uploaded files if bug creation fails
    if (req.files) {
      req.files.forEach(file => {
        if (fs.existsSync(file.path)) {
          fs.unlinkSync(file.path);
        }
      });
    }

    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// READ: Get all bugs with filtering and pagination
router.get('/', async (req, res) => {
  try {
    const {
      status,
      priority,
      category,
      reporter,
      assignedTo,
      search,
      page,
      limit,
      sortBy,
      sortOrder
    } = req.query;

    const result = await BugService.getBugs(
      { status, priority, category, reporter, assignedTo, search },
      { page, limit, sortBy, sortOrder }
    );

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// READ: Get bug statistics
router.get('/stats', async (req, res) => {
  try {
    const { reporter, assignedTo, dateFrom, dateTo } = req.query;
    const stats = await BugService.getBugStats({ reporter, assignedTo, dateFrom, dateTo });

    res.json({
      success: true,
      stats
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// READ: Get specific bug by ID
router.get('/:bugId', async (req, res) => {
  try {
    const bug = await BugService.getBugById(req.params.bugId);

    res.json({
      success: true,
      bug
    });
  } catch (error) {
    res.status(404).json({
      success: false,
      message: error.message
    });
  }
});

// UPDATE: Update bug report
router.put('/:bugId', upload.array('attachments', 5), async (req, res) => {
  try {
    const updateData = { ...req.body };
    const userId = req.body.userId || req.user?.id; // Use authenticated user ID when auth is implemented

    // Handle new attachments
    if (req.files && req.files.length > 0) {
      const newAttachments = req.files.map(file => ({
        filename: file.filename,
        originalName: file.originalname,
        path: file.path,
        size: file.size,
        mimetype: file.mimetype
      }));

      // Merge with existing attachments if specified
      if (updateData.keepExistingAttachments === 'true') {
        const existingBug = await BugService.getBugById(req.params.bugId);
        updateData.attachments = [...(existingBug.attachments || []), ...newAttachments];
      } else {
        updateData.attachments = newAttachments;
      }
    }

    // Remove keepExistingAttachments from update data
    delete updateData.keepExistingAttachments;

    const bug = await BugService.updateBug(req.params.bugId, updateData, userId);

    res.json({
      success: true,
      message: 'Bug report updated successfully',
      bug
    });
  } catch (error) {
    // Clean up uploaded files if update fails
    if (req.files) {
      req.files.forEach(file => {
        if (fs.existsSync(file.path)) {
          fs.unlinkSync(file.path);
        }
      });
    }

    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// UPDATE: Assign bug to user
router.put('/:bugId/assign', async (req, res) => {
  try {
    const { assignedTo } = req.body;
    const userId = req.body.userId || req.user?.id;

    if (!assignedTo) {
      return res.status(400).json({
        success: false,
        message: 'assignedTo field is required'
      });
    }

    const bug = await BugService.assignBug(req.params.bugId, assignedTo, userId);

    res.json({
      success: true,
      message: 'Bug assigned successfully',
      bug
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// DELETE: Delete bug report
router.delete('/:bugId', async (req, res) => {
  try {
    const userId = req.body.userId || req.user?.id;
    const result = await BugService.deleteBug(req.params.bugId, userId);

    res.json(result);
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// COMMENTS ROUTES

// CREATE: Add comment to bug
router.post('/:bugId/comments', validateComment, async (req, res) => {
  try {
    const commentData = {
      ...req.body,
      author: req.body.author || req.user?.id
    };

    const comment = await CommentService.addComment(req.params.bugId, commentData);

    res.status(201).json({
      success: true,
      message: 'Comment added successfully',
      comment
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// READ: Get comments for bug
router.get('/:bugId/comments', async (req, res) => {
  try {
    const { page, limit, sortBy, sortOrder } = req.query;
    const result = await CommentService.getComments(req.params.bugId, {
      page, limit, sortBy, sortOrder
    });

    res.json({
      success: true,
      ...result
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// READ: Get comment statistics for bug
router.get('/:bugId/comments/stats', async (req, res) => {
  try {
    const stats = await CommentService.getCommentStats(req.params.bugId);

    res.json({
      success: true,
      stats
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// UPDATE: Update comment
router.put('/comments/:commentId', async (req, res) => {
  try {
    const userId = req.body.userId || req.user?.id;
    const comment = await CommentService.updateComment(req.params.commentId, req.body, userId);

    res.json({
      success: true,
      message: 'Comment updated successfully',
      comment
    });
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// DELETE: Delete comment
router.delete('/comments/:commentId', async (req, res) => {
  try {
    const userId = req.body.userId || req.user?.id;
    const result = await CommentService.deleteComment(req.params.commentId, userId);

    res.json(result);
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

// Serve uploaded files
router.get('/attachments/:filename', (req, res) => {
  try {
    const filename = req.params.filename;
    const filepath = path.join(__dirname, '../../uploads/bugs', filename);

    if (!fs.existsSync(filepath)) {
      return res.status(404).json({
        success: false,
        message: 'File not found'
      });
    }

    res.sendFile(path.resolve(filepath));
  } catch (error) {
    res.status(400).json({
      success: false,
      message: error.message
    });
  }
});

module.exports = router;