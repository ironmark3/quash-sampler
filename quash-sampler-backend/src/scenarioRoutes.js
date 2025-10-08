const express = require('express');

const router = express.Router();

router.get('/status/ok', (req, res) => {
  res.json({
    success: true,
    message: 'Everything is operating normally',
    timestamp: new Date().toISOString()
  });
});

router.get('/status/not-found', (req, res) => {
  res.status(404).json({
    success: false,
    message: 'The resource you requested was not found',
    code: 'NOT_FOUND'
  });
});

router.get('/status/server-error', (req, res) => {
  res.status(500).json({
    success: false,
    message: 'Unexpected error occurred',
    code: 'INTERNAL_ERROR'
  });
});

router.get('/metrics/daily', (req, res) => {
  res.json({
    success: true,
    generatedAt: new Date().toISOString(),
    data: {
      totals: {
        signups: 128,
        otpRequests: 342,
        successfulSessions: 317
      },
      latestOtp: {
        code: '232423',
        channel: 'sms',
        sessionId: 'session_demo_123',
        issuedAt: new Date(Date.now() - 60 * 1000).toISOString()
      },
      recentSessions: [
        {
          id: 'session_demo_120',
          status: 'verified',
          latencyMs: 842,
          identifier: '+12025550120'
        },
        {
          id: 'session_demo_121',
          status: 'pending',
          latencyMs: 1295,
          identifier: 'user@example.com'
        }
      ]
    }
  });
});

router.get('/delayed', async (req, res) => {
  const delayMs = Math.min(Math.max(Number(req.query.ms) || 1500, 0), 10000);
  await new Promise((resolve) => setTimeout(resolve, delayMs));
  res.json({
    success: true,
    delayMs,
    message: `Responded after ${delayMs}ms`
  });
});

router.post('/orders', (req, res) => {
  const { productId, quantity, channel } = req.body || {};
  if (!productId || typeof productId !== 'string') {
    return res.status(422).json({
      success: false,
      message: 'productId is required',
      field: 'productId'
    });
  }
  const parsedQuantity = Number(quantity);
  if (!Number.isFinite(parsedQuantity) || parsedQuantity <= 0) {
    return res.status(422).json({
      success: false,
      message: 'quantity must be a positive number',
      field: 'quantity'
    });
  }
  res.status(201).json({
    success: true,
    order: {
      id: `order_${Date.now()}`,
      productId,
      quantity: parsedQuantity,
      channel: channel || 'app',
      status: 'received'
    }
  });
});

router.get('/otp/latest', (req, res) => {
  res.type('text/plain').send('Latest OTP is 232423 for session session_demo_123');
});

// ==================== 2xx Success Responses ====================

// POST /api/status/201 - Created with Location header
router.post('/status/201', (req, res) => {
  const resourceId = `resource_${Date.now()}`;
  res.status(201)
    .location(`/api/resources/${resourceId}`)
    .json({
      success: true,
      message: 'Resource created successfully',
      id: resourceId,
      createdAt: new Date().toISOString()
    });
});

// PUT /api/status/204 - No Content
router.put('/status/204', (req, res) => {
  res.status(204).send();
});

// GET /api/status/206 - Partial Content
router.get('/status/206', (req, res) => {
  const fullContent = 'This is the full content of the resource that can be partially retrieved.';
  const start = 0;
  const end = 20;
  const partial = fullContent.substring(start, end);

  res.status(206)
    .set({
      'Content-Range': `bytes ${start}-${end}/${fullContent.length}`,
      'Content-Type': 'text/plain'
    })
    .send(partial);
});

// ==================== 3xx Redirect Responses ====================

// GET /api/redirect/301 - Permanent Redirect
router.get('/redirect/301', (req, res) => {
  res.status(301)
    .location('/api/status/ok')
    .json({
      success: true,
      message: 'Resource has been moved permanently',
      redirectTo: '/api/status/ok'
    });
});

// GET /api/redirect/302 - Temporary Redirect
router.get('/redirect/302', (req, res) => {
  res.status(302)
    .location('/api/status/ok')
    .json({
      success: true,
      message: 'Resource temporarily redirected',
      redirectTo: '/api/status/ok'
    });
});

// GET /api/redirect/304 - Not Modified
router.get('/redirect/304', (req, res) => {
  const etag = '"abc123"';
  const clientEtag = req.headers['if-none-match'];

  if (clientEtag === etag) {
    res.status(304).send();
  } else {
    res.status(200)
      .set('ETag', etag)
      .json({
        success: true,
        message: 'Resource content',
        data: { value: 'sample data' }
      });
  }
});

// ==================== 4xx Client Error Responses ====================

// GET /api/status/400 - Bad Request
router.get('/status/400', (req, res) => {
  res.status(400).json({
    success: false,
    message: 'The request was malformed or invalid',
    code: 'BAD_REQUEST',
    errors: [
      { field: 'email', message: 'Invalid email format' }
    ]
  });
});

// GET /api/status/401 - Unauthorized
router.get('/status/401', (req, res) => {
  res.status(401)
    .set('WWW-Authenticate', 'Bearer realm="api"')
    .json({
      success: false,
      message: 'Authentication required to access this resource',
      code: 'UNAUTHORIZED'
    });
});

// GET /api/status/403 - Forbidden
router.get('/status/403', (req, res) => {
  res.status(403).json({
    success: false,
    message: 'You do not have permission to access this resource',
    code: 'FORBIDDEN',
    requiredRole: 'admin'
  });
});

// GET /api/status/429 - Too Many Requests (Rate Limited)
router.get('/status/429', (req, res) => {
  const retryAfter = 60; // seconds
  res.status(429)
    .set('Retry-After', retryAfter.toString())
    .set('X-RateLimit-Limit', '100')
    .set('X-RateLimit-Remaining', '0')
    .set('X-RateLimit-Reset', Math.floor(Date.now() / 1000 + retryAfter).toString())
    .json({
      success: false,
      message: 'Too many requests. Please try again later.',
      code: 'RATE_LIMIT_EXCEEDED',
      retryAfter: retryAfter
    });
});

// ==================== 5xx Server Error Responses ====================

// GET /api/status/502 - Bad Gateway
router.get('/status/502', (req, res) => {
  res.status(502).json({
    success: false,
    message: 'The upstream server returned an invalid response',
    code: 'BAD_GATEWAY',
    upstreamService: 'payment-service'
  });
});

// GET /api/status/503 - Service Unavailable
router.get('/status/503', (req, res) => {
  const retryAfter = 120; // seconds
  res.status(503)
    .set('Retry-After', retryAfter.toString())
    .json({
      success: false,
      message: 'Service is temporarily unavailable due to maintenance',
      code: 'SERVICE_UNAVAILABLE',
      retryAfter: retryAfter,
      maintenanceWindow: {
        start: new Date().toISOString(),
        estimatedEnd: new Date(Date.now() + retryAfter * 1000).toISOString()
      }
    });
});

// GET /api/status/504 - Gateway Timeout
router.get('/status/504', (req, res) => {
  res.status(504).json({
    success: false,
    message: 'The upstream server failed to respond in time',
    code: 'GATEWAY_TIMEOUT',
    upstreamService: 'database',
    timeout: '30s'
  });
});

// ==================== Query Parameters & Filters ====================

// GET /api/query/simple - Single query parameter
router.get('/query/simple', (req, res) => {
  const { key } = req.query;

  if (!key) {
    return res.status(400).json({
      success: false,
      message: 'No query parameters provided',
      code: 'NO_PARAMS'
    });
  }

  res.json({
    success: true,
    params: { key },
    count: 1,
    receivedAt: new Date().toISOString()
  });
});

// GET /api/query/multiple - Multiple query parameters
router.get('/query/multiple', (req, res) => {
  const params = req.query;
  const paramCount = Object.keys(params).length;

  if (paramCount === 0) {
    return res.status(400).json({
      success: false,
      message: 'No query parameters provided',
      code: 'NO_PARAMS'
    });
  }

  res.json({
    success: true,
    params: params,
    count: paramCount,
    keys: Object.keys(params)
  });
});

// GET /api/query/array - Array query parameters
router.get('/query/array', (req, res) => {
  const params = req.query;
  const arrayParams = {};
  const scalarParams = {};

  Object.keys(params).forEach(key => {
    if (Array.isArray(params[key])) {
      arrayParams[key] = params[key];
    } else {
      scalarParams[key] = params[key];
    }
  });

  res.json({
    success: true,
    arrayParams,
    scalarParams,
    arrayCount: Object.keys(arrayParams).length,
    totalParams: Object.keys(params).length
  });
});

// GET /api/query/special-chars - Special characters in query params
router.get('/query/special-chars', (req, res) => {
  const { q } = req.query;

  if (!q) {
    return res.status(400).json({
      success: false,
      message: 'Query parameter "q" is required',
      code: 'MISSING_PARAM'
    });
  }

  // Show URL encoding info
  const specialChars = {
    space: ' ',
    exclamation: '!',
    hash: '#',
    dollar: '$',
    percent: '%',
    ampersand: '&',
    plus: '+'
  };

  res.json({
    success: true,
    decoded: q,
    length: q.length,
    containsSpecialChars: /[^a-zA-Z0-9]/.test(q),
    specialCharsReference: specialChars
  });
});

// GET /api/query/missing-required - Missing required parameter
router.get('/query/missing-required', (req, res) => {
  const { id } = req.query;

  if (!id) {
    return res.status(400).json({
      success: false,
      message: 'Missing required parameter: id',
      code: 'MISSING_PARAMETER',
      requiredParams: ['id']
    });
  }

  res.json({
    success: true,
    message: 'Required parameter provided',
    id
  });
});

// ==================== Headers Testing ====================

// GET /api/headers/echo - Echo all request headers
router.get('/headers/echo', (req, res) => {
  const headers = req.headers;

  // Filter out some internal headers for cleaner output
  const clientHeaders = { ...headers };

  res.json({
    success: true,
    headers: clientHeaders,
    count: Object.keys(clientHeaders).length,
    headerNames: Object.keys(clientHeaders)
  });
});

// GET /api/headers/required - Require specific header
router.get('/headers/required', (req, res) => {
  const apiKey = req.headers['x-api-key'];

  if (!apiKey) {
    return res.status(400).json({
      success: false,
      message: 'Missing required header: X-API-Key',
      code: 'MISSING_HEADER',
      requiredHeaders: ['X-API-Key']
    });
  }

  res.json({
    success: true,
    message: 'Required header provided',
    apiKey: apiKey
  });
});

// GET /api/headers/custom - Return custom headers
router.get('/headers/custom', (req, res) => {
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

  res.set({
    'X-Custom-Header': 'Custom-Value',
    'X-Request-Id': requestId,
    'X-Rate-Limit': '100',
    'X-Rate-Limit-Remaining': '99',
    'X-Server-Version': '1.0.0',
    'X-Response-Time': '42ms'
  });

  res.json({
    success: true,
    message: 'Custom headers set in response',
    customHeaders: {
      'X-Custom-Header': 'Custom-Value',
      'X-Request-Id': requestId,
      'X-Rate-Limit': '100',
      'X-Rate-Limit-Remaining': '99',
      'X-Server-Version': '1.0.0',
      'X-Response-Time': '42ms'
    }
  });
});

// GET /api/headers/case-sensitive - Test header case sensitivity
router.get('/headers/case-sensitive', (req, res) => {
  // HTTP headers are case-insensitive
  const testHeader = req.headers['x-test-header'];

  res.json({
    success: true,
    message: 'Headers are case-insensitive in HTTP',
    headerValue: testHeader || null,
    testedCases: ['x-test-header', 'X-Test-Header', 'X-TEST-HEADER'],
    note: 'All variations of the header name will return the same value'
  });
});

// GET /api/headers/compression - gzip/deflate response
router.get('/headers/compression', (req, res) => {
  const acceptEncoding = req.headers['accept-encoding'];

  // Generate large payload to demonstrate compression
  const largeData = {
    success: true,
    message: 'This response can be compressed',
    acceptEncoding: acceptEncoding || 'none',
    supportsCompression: acceptEncoding ? acceptEncoding.includes('gzip') || acceptEncoding.includes('deflate') : false,
    data: Array(100).fill(null).map((_, i) => ({
      id: i + 1,
      name: `Item ${i + 1}`,
      description: `This is a sample item description for item number ${i + 1}`,
      timestamp: new Date().toISOString()
    }))
  };

  res.json(largeData);
});

// ==================== Timing & Performance ====================

// GET /api/delay - Configurable delay (alias for /delayed)
router.get('/delay', async (req, res) => {
  const delayMs = Math.min(Math.max(Number(req.query.ms) || 1000, 0), 10000);

  const startTime = Date.now();
  await new Promise((resolve) => setTimeout(resolve, delayMs));
  const endTime = Date.now();
  const actualDelay = endTime - startTime;

  res.json({
    success: true,
    requestedDelay: delayMs,
    actualDelay: actualDelay,
    message: `Responded after ${actualDelay}ms`
  });
});

// GET /api/timeout - Never responds (test timeouts)
router.get('/timeout', (req, res) => {
  // Never send response - let client timeout
  // Keep connection open but don't respond
  console.log(`⏱️  Timeout endpoint hit - connection will hang`);
  // Don't call res.send() or res.json()
});

// GET /api/slow-stream - Chunked slow response
router.get('/slow-stream', async (req, res) => {
  const chunks = parseInt(req.query.chunks) || 10;
  const delayMs = parseInt(req.query.delay) || 500;

  res.setHeader('Content-Type', 'text/plain');
  res.setHeader('Transfer-Encoding', 'chunked');

  res.write('Starting slow stream...\n\n');

  for (let i = 0; i < chunks; i++) {
    await new Promise(resolve => setTimeout(resolve, delayMs));
    res.write(`Chunk ${i + 1}/${chunks} - Timestamp: ${new Date().toISOString()}\n`);
  }

  res.write('\nStream completed!');
  res.end();
});

// GET /api/fast - Instant response
router.get('/fast', (req, res) => {
  res.json({
    success: true,
    message: 'Fast response',
    timestamp: new Date().toISOString()
  });
});

// ==================== Pagination ====================

// Helper function to generate mock data
function generateMockData(count = 100) {
  return Array(count).fill(null).map((_, i) => ({
    id: i + 1,
    name: `Item ${i + 1}`,
    description: `Description for item ${i + 1}`,
    createdAt: new Date(Date.now() - (count - i) * 86400000).toISOString(),
    status: i % 3 === 0 ? 'active' : i % 3 === 1 ? 'pending' : 'completed'
  }));
}

// GET /api/pagination/offset - Offset-based pagination
router.get('/pagination/offset', (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const limit = Math.min(parseInt(req.query.limit) || 10, 100); // Max 100 per page

  const allData = generateMockData(100);
  const totalItems = allData.length;
  const totalPages = Math.ceil(totalItems / limit);

  // Validate page number
  if (page < 1) {
    return res.status(400).json({
      success: false,
      message: 'Page number must be >= 1',
      code: 'INVALID_PAGE'
    });
  }

  const offset = (page - 1) * limit;
  const paginatedData = allData.slice(offset, offset + limit);

  res.json({
    success: true,
    data: paginatedData,
    pagination: {
      page,
      limit,
      totalItems,
      totalPages,
      hasNext: page < totalPages,
      hasPrevious: page > 1,
      nextPage: page < totalPages ? page + 1 : null,
      previousPage: page > 1 ? page - 1 : null
    }
  });
});

// GET /api/pagination/cursor - Cursor-based pagination
router.get('/pagination/cursor', (req, res) => {
  const cursor = req.query.cursor;
  const limit = Math.min(parseInt(req.query.limit) || 10, 100);

  const allData = generateMockData(100);

  let startIndex = 0;
  if (cursor) {
    // Find the position based on cursor (using ID as cursor)
    const cursorId = parseInt(cursor);
    startIndex = allData.findIndex(item => item.id === cursorId);

    if (startIndex === -1) {
      return res.status(400).json({
        success: false,
        message: 'Invalid cursor',
        code: 'INVALID_CURSOR'
      });
    }

    startIndex += 1; // Start from next item
  }

  const paginatedData = allData.slice(startIndex, startIndex + limit);
  const hasMore = startIndex + limit < allData.length;
  const nextCursor = hasMore ? paginatedData[paginatedData.length - 1].id : null;

  res.json({
    success: true,
    data: paginatedData,
    pagination: {
      cursor: cursor || null,
      limit,
      nextCursor,
      hasMore,
      itemsReturned: paginatedData.length
    }
  });
});

// GET /api/pagination/link-header - RFC 5988 Link headers
router.get('/pagination/link-header', (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 10;

  const allData = generateMockData(100);
  const totalItems = allData.length;
  const totalPages = Math.ceil(totalItems / limit);

  const offset = (page - 1) * limit;
  const paginatedData = allData.slice(offset, offset + limit);

  // Build Link header (RFC 5988)
  const baseUrl = `${req.protocol}://${req.get('host')}${req.path}`;
  const links = [];

  links.push(`<${baseUrl}?page=1&limit=${limit}>; rel="first"`);

  if (page > 1) {
    links.push(`<${baseUrl}?page=${page - 1}&limit=${limit}>; rel="prev"`);
  }

  if (page < totalPages) {
    links.push(`<${baseUrl}?page=${page + 1}&limit=${limit}>; rel="next"`);
  }

  links.push(`<${baseUrl}?page=${totalPages}&limit=${limit}>; rel="last"`);

  res.set('Link', links.join(', '));

  res.json({
    success: true,
    data: paginatedData,
    page,
    totalPages,
    totalItems,
    itemsPerPage: limit
  });
});

// ==================== Edge Cases & Special Scenarios ====================

// Helper function to get random status code
function getRandomStatusCode() {
  const statuses = [200, 201, 400, 404, 500, 503];
  return statuses[Math.floor(Math.random() * statuses.length)];
}

// Helper function to generate random data
function generateRandomData() {
  const types = ['object', 'array', 'nested'];
  const type = types[Math.floor(Math.random() * types.length)];

  if (type === 'object') {
    return {
      id: Math.floor(Math.random() * 1000),
      name: `Item_${Math.random().toString(36).substr(2, 9)}`,
      value: Math.random() * 100,
      active: Math.random() > 0.5,
      timestamp: new Date().toISOString()
    };
  } else if (type === 'array') {
    const length = Math.floor(Math.random() * 20) + 1;
    return Array(length).fill(null).map((_, i) => ({
      index: i,
      value: Math.random().toString(36).substr(2, 9)
    }));
  } else {
    return {
      level1: {
        data: Math.random().toString(36).substr(2, 9),
        level2: {
          data: Math.random().toString(36).substr(2, 9),
          level3: {
            value: Math.floor(Math.random() * 1000)
          }
        }
      }
    };
  }
}

// Helper function to parse size string to bytes
function parseSizeToBytes(sizeStr) {
  const units = {
    'b': 1,
    'kb': 1024,
    'mb': 1024 * 1024,
    'gb': 1024 * 1024 * 1024
  };

  const match = sizeStr.toLowerCase().match(/^(\d+(?:\.\d+)?)(b|kb|mb|gb)$/);
  if (!match) return 1024; // Default 1KB

  const value = parseFloat(match[1]);
  const unit = match[2];
  return Math.floor(value * units[unit]);
}

// Helper function to generate nested object
function generateNestedObject(depth, currentDepth = 1) {
  if (currentDepth >= depth) {
    return {
      level: currentDepth,
      value: `Deepest level reached`,
      timestamp: new Date().toISOString()
    };
  }

  return {
    level: currentDepth,
    message: `Level ${currentDepth} of ${depth}`,
    data: generateNestedObject(depth, currentDepth + 1)
  };
}

// GET /api/random/response - Random status code
router.get('/random/response', (req, res) => {
  const randomStatus = getRandomStatusCode();

  res.status(randomStatus)
    .set('X-Random-Status', randomStatus.toString())
    .json({
      success: randomStatus < 400,
      message: `Randomly generated ${randomStatus} response`,
      statusCode: randomStatus,
      timestamp: new Date().toISOString(),
      note: 'Each request returns a different random status code'
    });
});

// GET /api/random/data - Random data generation
router.get('/random/data', (req, res) => {
  const data = generateRandomData();
  const dataType = Array.isArray(data) ? 'array' : typeof data === 'object' ? 'object' : typeof data;

  res.json({
    success: true,
    dataType: dataType,
    itemCount: Array.isArray(data) ? data.length : Object.keys(data).length,
    data: data,
    generatedAt: new Date().toISOString()
  });
});

// GET /api/flaky - Intermittent failures
router.get('/flaky', (req, res) => {
  const failureRate = Math.min(Math.max(parseFloat(req.query.failure_rate) || 0.3, 0), 1);
  const shouldFail = Math.random() < failureRate;

  if (shouldFail) {
    res.status(500)
      .set('X-Flaky-Result', 'failure')
      .set('X-Failure-Rate', failureRate.toString())
      .json({
        success: false,
        message: 'Simulated intermittent failure',
        code: 'FLAKY_ERROR',
        failureRate: failureRate,
        timestamp: new Date().toISOString()
      });
  } else {
    res.status(200)
      .set('X-Flaky-Result', 'success')
      .set('X-Failure-Rate', failureRate.toString())
      .json({
        success: true,
        message: 'Request succeeded this time',
        failureRate: failureRate,
        timestamp: new Date().toISOString()
      });
  }
});

// GET /api/large-response - Configurable response size
router.get('/large-response', (req, res) => {
  const sizeParam = req.query.size || '1kb';
  const targetBytes = parseSizeToBytes(sizeParam);

  // Generate data to fill the target size
  const items = [];
  let currentSize = 0;
  let index = 0;

  const baseJson = JSON.stringify({
    success: true,
    message: 'Large response generated',
    requestedSize: sizeParam,
    items: []
  });
  currentSize = baseJson.length;

  while (currentSize < targetBytes) {
    const item = {
      id: index++,
      name: `Item ${index}`,
      description: `This is a sample item with some text to fill up space. Item number ${index}.`,
      timestamp: new Date().toISOString(),
      data: 'x'.repeat(100) // Filler data
    };
    items.push(item);
    currentSize += JSON.stringify(item).length;
  }

  const response = {
    success: true,
    message: 'Large response generated',
    requestedSize: sizeParam,
    targetBytes: targetBytes,
    itemsGenerated: items.length,
    items: items
  };

  const actualSize = JSON.stringify(response).length;

  res.set('X-Generated-Size', actualSize.toString())
    .set('Content-Length', actualSize.toString())
    .json(response);
});

// GET /api/nested-deep - Deeply nested JSON
router.get('/nested-deep', (req, res) => {
  const depth = parseInt(req.query.depth) || 10;
  const maxDepth = Math.min(depth, 50); // Limit to 50 levels max

  const nestedData = generateNestedObject(maxDepth);

  res.set('X-Nesting-Depth', maxDepth.toString())
    .json({
      success: true,
      message: `Generated deeply nested JSON with ${maxDepth} levels`,
      depth: maxDepth,
      nested: nestedData
    });
});

// GET /api/empty-array - Empty array response
router.get('/empty-array', (req, res) => {
  res.set('X-Result-Count', '0')
    .json([]);
});

// GET /api/null-response - Null response
router.get('/null-response', (req, res) => {
  res.set('X-Response-Type', 'null')
    .json(null);
});

// GET /api/unicode - Unicode/emoji data
router.get('/unicode', (req, res) => {
  res.json({
    success: true,
    message: 'Unicode and emoji data 🚀',
    emojis: {
      celebration: '🎉',
      rocket: '🚀',
      light: '💡',
      lightning: '⚡',
      star: '🌟',
      heart: '❤️',
      check: '✅',
      fire: '🔥'
    },
    international: {
      chinese: '你好世界',
      arabic: 'مرحبا بالعالم',
      hindi: 'नमस्ते दुनिया',
      japanese: 'こんにちは世界',
      korean: '안녕하세요 세계',
      russian: 'Привет мир'
    },
    mathematical: {
      summation: '∑',
      integral: '∫',
      squareRoot: '√',
      pi: 'π',
      infinity: '∞',
      delta: 'Δ',
      lambda: 'λ'
    },
    symbols: {
      copyright: '©',
      registered: '®',
      trademark: '™',
      currency: '€£¥₹',
      arrows: '←→↑↓'
    }
  });
});

// GET /api/special-chars - Special characters in response
router.get('/special-chars', (req, res) => {
  res.json({
    success: true,
    message: 'Response with special characters',
    examples: {
      quotes: {
        single: "It's a beautiful day",
        double: 'She said "Hello"',
        backtick: 'Template `literal` example'
      },
      backslashes: {
        path: 'C:\\Users\\Documents\\file.txt',
        regex: '\\d+\\s+\\w+'
      },
      newlines: {
        multiline: 'Line 1\nLine 2\nLine 3',
        withTabs: 'Column1\tColumn2\tColumn3'
      },
      html: {
        escaped: '&lt;div&gt;Content&lt;/div&gt;',
        entities: '&amp; &quot; &apos; &lt; &gt;'
      },
      control: {
        carriageReturn: 'Text with\rcarriage return',
        formFeed: 'Text with\fform feed',
        verticalTab: 'Text with\vvertical tab'
      },
      special: {
        mixed: "Mix'd \"special\" chars: <tag> & symbol's!"
      }
    }
  });
});

// ==================== CORS Testing ====================

// Helper function to set CORS headers
function setCorsHeaders(res, type, origin) {
  if (type === 'simple') {
    res.set({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
  } else if (type === 'credentials') {
    res.set({
      'Access-Control-Allow-Origin': origin || 'http://localhost:3000',
      'Access-Control-Allow-Credentials': 'true',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400'
    });
  } else if (type === 'custom-headers') {
    res.set({
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PATCH, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'X-Custom-Header, X-Api-Key, X-Request-ID, Content-Type',
      'Access-Control-Expose-Headers': 'X-Total-Count, X-Page-Number',
      'Access-Control-Max-Age': '3600'
    });
  }
}

// OPTIONS /api/cors/simple - Simple CORS
router.options('/cors/simple', (req, res) => {
  setCorsHeaders(res, 'simple');
  res.status(204).send();
});

// GET /api/cors/simple - Simple CORS (actual request)
router.get('/cors/simple', (req, res) => {
  setCorsHeaders(res, 'simple');
  res.json({
    success: true,
    message: 'Simple CORS enabled',
    allowedOrigins: '*',
    allowedMethods: ['GET', 'POST', 'OPTIONS']
  });
});

// OPTIONS /api/cors/credentials - CORS with credentials
router.options('/cors/credentials', (req, res) => {
  const origin = req.headers.origin;
  setCorsHeaders(res, 'credentials', origin);
  res.status(204).send();
});

// GET /api/cors/credentials - CORS with credentials (actual request)
router.get('/cors/credentials', (req, res) => {
  const origin = req.headers.origin;
  setCorsHeaders(res, 'credentials', origin);
  res.json({
    success: true,
    message: 'CORS with credentials enabled',
    origin: origin || 'http://localhost:3000',
    credentialsAllowed: true,
    note: 'This endpoint allows cookies and authentication headers'
  });
});

// OPTIONS /api/cors/custom-headers - CORS with custom headers
router.options('/cors/custom-headers', (req, res) => {
  setCorsHeaders(res, 'custom-headers');
  res.status(204).send();
});

// GET /api/cors/custom-headers - CORS with custom headers (actual request)
router.get('/cors/custom-headers', (req, res) => {
  setCorsHeaders(res, 'custom-headers');

  // Set some exposed headers
  res.set({
    'X-Total-Count': '100',
    'X-Page-Number': '1'
  });

  res.json({
    success: true,
    message: 'CORS with custom headers enabled',
    allowedHeaders: ['X-Custom-Header', 'X-Api-Key', 'X-Request-ID', 'Content-Type'],
    exposedHeaders: ['X-Total-Count', 'X-Page-Number'],
    data: {
      totalCount: 100,
      pageNumber: 1
    }
  });
});

module.exports = router;
