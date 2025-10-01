const { User } = require('../models');

class UserService {
  // Find or create user during OTP verification
  static async findOrCreateUser(identifier) {
    try {
      const isEmail = identifier.includes('@');
      const query = isEmail ? { email: identifier } : { phone: identifier };

      // Try to find existing user
      let user = await User.findOne(query);

      if (!user) {
        // Create new user with basic info
        const userData = {
          name: 'New User', // Placeholder name
          email: isEmail ? identifier : null,
          phone: isEmail ? null : identifier,
          isProfileComplete: false
        };

        user = new User(userData);
        await user.save();

        console.log(`✅ Created new user: ${user._id}`);
      } else {
        console.log(`✅ Found existing user: ${user._id}`);
      }

      return user;
    } catch (error) {
      console.error('Error in findOrCreateUser:', error);
      throw error;
    }
  }

  // Get user by ID
  static async getUserById(userId) {
    try {
      return await User.findById(userId);
    } catch (error) {
      console.error('Error in getUserById:', error);
      throw error;
    }
  }

  // Update user profile
  static async updateUserProfile(userId, profileData) {
    try {
      const user = await User.findByIdAndUpdate(
        userId,
        { $set: profileData },
        { new: true, runValidators: true }
      );

      if (!user) {
        throw new Error('User not found');
      }

      return user;
    } catch (error) {
      console.error('Error in updateUserProfile:', error);
      throw error;
    }
  }

  // Check if user profile is complete
  static async checkProfileCompletion(userId) {
    try {
      const user = await User.findById(userId);
      if (!user) {
        throw new Error('User not found');
      }

      return {
        isComplete: user.isProfileComplete,
        completionPercentage: user.profileCompletionPercentage,
        missingFields: this.getMissingFields(user)
      };
    } catch (error) {
      console.error('Error in checkProfileCompletion:', error);
      throw error;
    }
  }

  // Get missing profile fields
  static getMissingFields(user) {
    const requiredFields = ['name', 'email', 'phone', 'address', 'dateOfBirth', 'role'];
    const missingFields = [];

    requiredFields.forEach(field => {
      if (!user[field] || (typeof user[field] === 'string' && user[field].trim() === '')) {
        missingFields.push(field);
      }
    });

    return missingFields;
  }

  // Get users by role (for assignment purposes)
  static async getUsersByRole(role) {
    try {
      return await User.find({ role }).select('_id name email role');
    } catch (error) {
      console.error('Error in getUsersByRole:', error);
      throw error;
    }
  }

  // Search users by name or email
  static async searchUsers(searchTerm, limit = 10) {
    try {
      const searchRegex = new RegExp(searchTerm, 'i');
      return await User.find({
        $or: [
          { name: searchRegex },
          { email: searchRegex }
        ]
      })
      .select('_id name email role')
      .limit(limit);
    } catch (error) {
      console.error('Error in searchUsers:', error);
      throw error;
    }
  }

  // Get user statistics
  static async getUserStats() {
    try {
      const stats = await User.aggregate([
        {
          $group: {
            _id: null,
            totalUsers: { $sum: 1 },
            completeProfiles: {
              $sum: { $cond: [{ $eq: ['$isProfileComplete', true] }, 1, 0] }
            },
            usersByRole: {
              $push: '$role'
            }
          }
        },
        {
          $project: {
            _id: 0,
            totalUsers: 1,
            completeProfiles: 1,
            incompleteProfiles: { $subtract: ['$totalUsers', '$completeProfiles'] },
            roleDistribution: {
              $reduce: {
                input: '$usersByRole',
                initialValue: { Reporter: 0, Developer: 0, QA: 0 },
                in: {
                  Reporter: {
                    $cond: [
                      { $eq: ['$$this', 'Reporter'] },
                      { $add: ['$$value.Reporter', 1] },
                      '$$value.Reporter'
                    ]
                  },
                  Developer: {
                    $cond: [
                      { $eq: ['$$this', 'Developer'] },
                      { $add: ['$$value.Developer', 1] },
                      '$$value.Developer'
                    ]
                  },
                  QA: {
                    $cond: [
                      { $eq: ['$$this', 'QA'] },
                      { $add: ['$$value.QA', 1] },
                      '$$value.QA'
                    ]
                  }
                }
              }
            }
          }
        }
      ]);

      return stats[0] || {
        totalUsers: 0,
        completeProfiles: 0,
        incompleteProfiles: 0,
        roleDistribution: { Reporter: 0, Developer: 0, QA: 0 }
      };
    } catch (error) {
      console.error('Error in getUserStats:', error);
      throw error;
    }
  }
}

module.exports = UserService;