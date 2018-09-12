import Qs from 'qs'
import axios from 'axios'
export const axiosPost = (opt) =>{
  return axios({
    method: 'post',
    url: opt.url,
    timeout: 120000,
    headers: {
      'Content-type': 'application/x-www-form-urlencoded;charset=UTF-8'
    },
    data: Qs.stringify(opt.data)
  })
};