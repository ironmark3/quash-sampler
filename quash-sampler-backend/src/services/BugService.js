const BugReport = require('../models/BugReport');
const Comment = require('../models/Comment');
const ActivityLog = require('../models/ActivityLog');
const User = require('../models/User');

class BugService {
  // Create a new bug report
  static async createBug(bugData) {
    try {
      const bug = new BugReport(bugData);
      await bug.save();

      // Populate reporter information
      await bug.populate('reporter', 'name email role');

      // Log activity
      await ActivityLog.create({
        userId: bugData.reporter,
        action: 'bug_created',
        details: `Created bug report: ${bug.title}`,
        metadata: { bugId: bug._id }
      });

      return bug;
    } catch (error) {
      throw new Error(`Failed to create bug report: ${error.message}`);
    }
  }

  // Get bugs with filtering and pagination
  static async getBugs(filters = {}, options = {}) {
    try {
      const {
        status,
        priority,
        category,
        reporter,
        assignedTo,
        search,
        page = 1,
        limit = 10,
        sortBy = 'createdAt',
        sortOrder = 'desc'
      } = { ...filters, ...options };

      // Build filter query
      const query = {};

      if (status) query.status = status;
      if (priority) query.priority = priority;
      if (category) query.category = category;
      if (reporter) query.reporter = reporter;
      if (assignedTo) query.assignedTo = assignedTo;

      if (search) {
        query.$or = [
          { title: { $regex: search, $options: 'i' } },
          { description: { $regex: search, $options: 'i' } },
          { bugId: { $regex: search, $options: 'i' } }
        ];
      }

      // Calculate pagination
      const skip = (page - 1) * limit;
      const sort = { [sortBy]: sortOrder === 'desc' ? -1 : 1 };

      // Get bugs with populated fields
      const bugs = await BugReport.find(query)
        .sort(sort)
        .skip(skip)
        .limit(limit)
        .populate('reporter', 'name email role')
        .populate('assignedTo', 'name email role')
        .lean();

      // Get total count for pagination
      const total = await BugReport.countDocuments(query);

      return {
        bugs,
        pagination: {
          page: parseInt(page),
          limit: parseInt(limit),
          total,
          pages: Math.ceil(total / limit)
        }
      };
    } catch (error) {
      throw new Error(`Failed to fetch bugs: ${error.message}`);
    }
  }

  // Get bug by ID
  static async getBugById(bugId) {
    try {
      const bug = await BugReport.findById(bugId)
        .populate('reporter', 'name email role')
        .populate('assignedTo', 'name email role');

      if (!bug) {
        throw new Error('Bug report not found');
      }

      return bug;
    } catch (error) {
      throw new Error(`Failed to fetch bug: ${error.message}`);
    }
  }

  // Update bug report
  static async updateBug(bugId, updateData, userId) {
    try {
      const bug = await BugReport.findById(bugId);
      if (!bug) {
        throw new Error('Bug report not found');
      }

      // Store original status for activity logging
      const originalStatus = bug.status;

      // Update bug fields
      Object.keys(updateData).forEach(key => {
        if (updateData[key] !== undefined) {
          bug[key] = updateData[key];
        }
      });

      bug.updatedAt = new Date();
      await bug.save();

      // Populate updated bug
      await bug.populate('reporter', 'name email role');
      await bug.populate('assignedTo', 'name email role');

      // Log activity for status changes
      if (originalStatus !== bug.status) {
        await ActivityLog.create({
          userId: userId,
          action: 'bug_status_changed',
          details: `Changed bug status from ${originalStatus} to ${bug.status}`,
          metadata: { bugId: bug._id, oldStatus: originalStatus, newStatus: bug.status }
        });
      }

      // Log general update activity
      await ActivityLog.create({
        userId: userId,
        action: 'bug_updated',
        details: `Updated bug report: ${bug.title}`,
        metadata: { bugId: bug._id, updatedFields: Object.keys(updateData) }
      });

      return bug;
    } catch (error) {
      throw new Error(`Failed to update bug: ${error.message}`);
    }
  }

  // Delete bug report
  static async deleteBug(bugId, userId) {
    try {
      const bug = await BugReport.findById(bugId);
      if (!bug) {
        throw new Error('Bug report not found');
      }

      // Check if user has permission to delete (reporter or admin role)
      const user = await User.findById(userId);
      if (bug.reporter.toString() !== userId && user.role !== 'Admin') {
        throw new Error('Permission denied: You can only delete your own bug reports');
      }

      await BugReport.findByIdAndDelete(bugId);

      // Log activity
      await ActivityLog.create({
        userId: userId,
        action: 'bug_deleted',
        details: `Deleted bug report: ${bug.title}`,
        metadata: { bugId: bug._id }
      });

      return { success: true, message: 'Bug report deleted successfully' };
    } catch (error) {
      throw new Error(`Failed to delete bug: ${error.message}`);
    }
  }

  // Assign bug to user
  static async assignBug(bugId, assignedToId, userId) {
    try {
      const bug = await BugReport.findById(bugId);
      if (!bug) {
        throw new Error('Bug report not found');
      }

      const assignedUser = await User.findById(assignedToId);
      if (!assignedUser) {
        throw new Error('Assigned user not found');
      }

      const previousAssignee = bug.assignedTo;
      bug.assignedTo = assignedToId;
      bug.updatedAt = new Date();
      await bug.save();

      await bug.populate('assignedTo', 'name email role');

      // Log activity
      await ActivityLog.create({
        userId: userId,
        action: 'bug_assigned',
        details: `Assigned bug to ${assignedUser.name}`,
        metadata: {
          bugId: bug._id,
          assignedTo: assignedToId,
          previousAssignee: previousAssignee
        }
      });

      return bug;
    } catch (error) {
      throw new Error(`Failed to assign bug: ${error.message}`);
    }
  }

  // Get bug statistics
  static async getBugStats(filters = {}) {
    try {
      const { reporter, assignedTo, dateFrom, dateTo } = filters;

      // Build base query
      const baseQuery = {};
      if (reporter) baseQuery.reporter = reporter;
      if (assignedTo) baseQuery.assignedTo = assignedTo;
      if (dateFrom || dateTo) {
        baseQuery.createdAt = {};
        if (dateFrom) baseQuery.createdAt.$gte = new Date(dateFrom);
        if (dateTo) baseQuery.createdAt.$lte = new Date(dateTo);
      }

      // Get total bugs
      const totalBugs = await BugReport.countDocuments(baseQuery);

      // Get status distribution
      const statusDistribution = await BugReport.aggregate([
        { $match: baseQuery },
        { $group: { _id: '$status', count: { $sum: 1 } } }
      ]);

      // Get priority distribution
      const priorityDistribution = await BugReport.aggregate([
        { $match: baseQuery },
        { $group: { _id: '$priority', count: { $sum: 1 } } }
      ]);

      // Get category distribution
      const categoryDistribution = await BugReport.aggregate([
        { $match: baseQuery },
        { $group: { _id: '$category', count: { $sum: 1 } } }
      ]);

      // Get recent bugs (last 7 days)
      const sevenDaysAgo = new Date();
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

      const recentBugs = await BugReport.countDocuments({
        ...baseQuery,
        createdAt: { $gte: sevenDaysAgo }
      });

      return {
        totalBugs,
        recentBugs,
        statusDistribution: statusDistribution.reduce((acc, item) => {
          acc[item._id] = item.count;
          return acc;
        }, {}),
        priorityDistribution: priorityDistribution.reduce((acc, item) => {
          acc[item._id] = item.count;
          return acc;
        }, {}),
        categoryDistribution: categoryDistribution.reduce((acc, item) => {
          acc[item._id] = item.count;
          return acc;
        }, {})
      };
    } catch (error) {
      throw new Error(`Failed to get bug statistics: ${error.message}`);
    }
  }
}

module.exports = BugService;