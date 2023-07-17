package com.volcengine.vertcdemo.videocall.util;

/**
 * 通用回调接口
 */
public interface Callback {
    /**
     * 结果数据封装
     *
     * @param <T> 结果数据集
     */
    class Result<T> {
        /***结果数据集*/
        public T result;
        /***操作成功还是失败*/
        public boolean success;

        public Result(boolean success) {
            this.success = success;
        }

        public Result(boolean success,T result) {
            this.success = success;
            this.result = result;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "result=" + result +
                    ", success=" + success +
                    '}';
        }
    }

    /**
     * 回调结果
     *
     * @param result 结果数据
     */
    void onResult(Result result);
}
