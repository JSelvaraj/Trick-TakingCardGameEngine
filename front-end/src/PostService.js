import axios from "axios";

const url = "http://localhost:8081";

class PostService {
  // Get Posts
  static getPosts() {
    return new Promise(async (resolve, reject) => {
      try {
        const res = await axios.get(url);
        const data = res.data;
        console.log(data)

        for(var subdata in data){
            var temp = ""
             for(var line in subdata){
              temp = subdata.id;
              console.log(temp);
            }
            
        }

        resolve(
          data.map(post => ({
            ...post,
          }))
        );
      } catch (err) {
        reject(err);
      }
    });
  }

  // Create Post
  static insertPosts(text) {
    return axios.post(url, {
      text
    });
  }

  // Delete Post
  static deletePost(id) {
    return axios.delete("${url}${id}");
  }
}

export default PostService;
