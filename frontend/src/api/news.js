import http from './http';

export const searchBlog = async (query, start = 1, display = 20) => {
    try {
        const response = await http.get(`/article/search/blog`, {
            params: {
                query,
                start,
                display
            }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
}; 

export const searchImage = async (query) => {
    try {
        const response = await http.get(`/article/search/image`, {
            params: {
                query: query
            }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const searchBlogWithImages = async (query, start, display) => {
  const response = await http.get(`/article/search/blog-with-images`, {
    params: { query, start, display }
  });
  return response.data;
};


export const searchVideo = async (query, maxResults = 10) => {
  try {
    const response = await http.get(`/article/search/video`, {
      params: {
        query,
        maxResults
      }
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getPlaylistVideos = async (playlistId, pageToken = '', maxResults = 50) => {
  try {
    const response = await http.get(`/article/playlist/videos`, {
      params: {
        playlistId,
        pageToken,
        maxResults
      }
    });
    
    return response.data;
  } catch (error) {
    throw error;
  }
};
