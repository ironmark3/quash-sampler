const Comment = require('../models/Comment');
const BugReport = require('../models/BugReport');
const ActivityLog = require('../models/ActivityLog');
const User = require('../models/User');

class CommentService {
  // Add comment to bug report
  static async addComment(bugId, commentData) {
    try {
      // Verify bug exists
      const bug = await BugReport.findById(bugId);
      if (!bug) {
        throw new Error('Bug report not found');
      }

      // Create comment
      const comment = new Comment({
        ...commentData,
        bugId: bugId
      });

      await comment.save();
      await comment.populate('author', 'name email role');

      // Log activity
      await ActivityLog.create({
        userId: commentData.author,
        action: 'comment_added',
        details: `Added comment to bug: ${bug.title}`,
        metadata: {
          bugId: bugId,
          commentId: comment._id
        }
      });

      return comment;
    } catch (error) {
      throw new Error(`Failed to add comment: ${error.message}`);
    }
  }

  // Get comments for a bug report
  static async getComments(bugId, options = {}) {
    try {
      const {
        page = 1,
        limit = 20,
        sortBy = 'createdAt',
        sortOrder = 'asc'
      } = options;

      // Verify bug exists
      const bug = await BugReport.findById(bugId);
      if (!bug) {
        throw new Error('Bug report not found');
      }

      // Calculate pagination
      const skip = (page - 1) * limit;
      const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

      // Get comments with populated author
      const comments = await Comment.find({ bugId })
        .sort(sort)
        .skip(skip)
        .limit(limit)
        .populate('author', 'name email role')
        .lean();

      // Get total count
      const total = await Comment.countDocuments({ bugId });

      return {
        comments,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          pages: Math.ceil(total / limit)
        }
      };
    } catch (error) {
      throw new Error(`Failed to fetch comments: ${error.message}`);
    }
  }

  // Get comment by ID
  static async getCommentById(commentId) {
    try {
      const comment = await Comment.findById(commentId)
        .populate('author', 'name email role');

      if (!comment) {
        throw new Error('Comment not found');
      }

      return comment;
    } catch (error) {
      throw new Error(`Failed to fetch comment: ${error.message}`);
    }
  }

  // Update comment
  static async updateComment(commentId, updateData, userId) {
    try {
      const comment = await Comment.findById(commentId);
      if (!comment) {
        throw new Error('Comment not found');
      }

      // Check if user has permission to update (author only)
      if (comment.author.toString() !== userId) {
        throw new Error('Permission denied: You can only update your own comments');
      }

      // Update comment fields
      Object.keys(updateData).forEach(key => {
        if (updateData[key] !== undefined && key !== 'author' && key !== 'bugId') {
          comment[key] = updateData[key];
        }
      });

      comment.updatedAt = new Date();
      await comment.save();

      await comment.populate('author', 'name email role');

      // Log activity
      await ActivityLog.create({
        userId: userId,
        action: 'comment_updated',
        details: `Updated comment on bug report`,
        metadata: {
          bugId: comment.bugId,
          commentId: comment._id
        }
      });

      return comment;
    } catch (error) {
      throw new Error(`Failed to update comment: ${error.message}`);
    }
  }

  // Delete comment
  static async deleteComment(commentId, userId) {
    try {
      const comment = await Comment.findById(commentId);
      if (!comment) {
        throw new Error('Comment not found');
      }

      // Check if user has permission to delete (author only, or admin in future)
      const user = await User.findById(userId);
      if (comment.author.toString() !== userId && user.role !== 'Admin') {
        throw new Error('Permission denied: You can only delete your own comments');
      }

      await Comment.findByIdAndDelete(commentId);

      // Log activity
      await ActivityLog.create({
        userId: userId,
        action: 'comment_deleted',
        details: `Deleted comment from bug report`,
        metadata: {
          bugId: comment.bugId,
          commentId: commentId
        }
      });

      return { success: true, message: 'Comment deleted successfully' };
    } catch (error) {
      throw new Error(`Failed to delete comment: ${error.message}`);
    }
  }

  // Get comment statistics for a bug
  static async getCommentStats(bugId) {
    try {
      // Verify bug exists
      const bug = await BugReport.findById(bugId);
      if (!bug) {
        throw new Error('Bug report not found');
      }

      // Get total comments
      const totalComments = await Comment.countDocuments({ bugId });

      // Get comments by author role
      const commentsByRole = await Comment.aggregate([
        { $match: { bugId: bug._id } },
        {
          $lookup: {
            from: 'users',
            localField: 'author',
            foreignField: '_id',
            as: 'authorInfo'
          }
        },
        { $unwind: '$authorInfo' },
        {
          $group: {
            _id: '$authorInfo.role',
            count: { $sum: 1 }
          }
        }
      ]);

      // Get recent activity (last 24 hours)
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);

      const recentComments = await Comment.countDocuments({
        bugId,
        createdAt: { $gte: yesterday }
      });

      return {
        totalComments,
        recentComments,
        commentsByRole: commentsByRole.reduce((acc, item) => {
          acc[item._id] = item.count;
          return acc;
        }, {})
      };
    } catch (error) {
      throw new Error(`Failed to get comment statistics: ${error.message}`);
    }
  }
}

module.exports = CommentService;