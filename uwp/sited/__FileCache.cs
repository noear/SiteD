using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Windows.Storage;

namespace org.noear.sited {
    class __FileCache : __ICache {
        static SemaphoreSlim semaphore = new SemaphoreSlim(1);

        StorageFolder folder;
        public __FileCache(string tag) {
            var root = ApplicationData.Current.LocalFolder;
            doInit(root, "sited");
        }

        async void doInit(StorageFolder root, string directory) {
            folder = await root.CreateFolderAsync(directory, CreationCollisionOption.OpenIfExists);
        }

        public async Task delete(string key) {
            if (folder != null) {
                String key_md5 = Util.md5(key);
                String path = key_md5.Substring(0, 2);

                await semaphore.WaitAsync();
                try {
                    StorageFolder dir2 = await folder.CreateFolderAsync(path, CreationCollisionOption.OpenIfExists);

                    var item = await dir2.TryGetItemAsync(key_md5);
                    if (item != null) {
                        await item.DeleteAsync(StorageDeleteOption.PermanentDelete);
                    }
                }
                catch (Exception ex) {
                    Debug.WriteLine(ex.Message, "ERROR " + "__FileCache.delete");
                }
                finally {
                    semaphore.Release();
                }
            }
        }

        public async Task<__CacheBlock> get(string key) {
            if (folder != null) {
                String key_md5 = Util.md5(key);
                String path = key_md5.Substring(0, 2);

                try {
                    StorageFolder dir2 = null;

                    await semaphore.WaitAsync();
                    try {
                        dir2 = await folder.CreateFolderAsync(path, CreationCollisionOption.OpenIfExists);
                    }
                    finally {
                        semaphore.Release();
                    }

                    if (dir2 == null)
                        return null;

                    var item = await dir2.TryGetItemAsync(key_md5);
                    if (item != null) {
                        var file = item as StorageFile;
                        if (file != null) {
                            __CacheBlock block = new __CacheBlock();
                            block.time = file.DateCreated.DateTime;
                            block.value = await FileIO.ReadTextAsync(file);

                            return block;
                        }
                    }
                }
                catch (Exception ex) {
                    Debug.WriteLine(ex.Message, "ERROR " + "__FileCache.get");
                }
            }

            return null;
        }

        public async Task<bool> isCached(string key) {
            if (folder != null) {
                String key_md5 = Util.md5(key);
                String path = key_md5.Substring(0, 2);

                try {
                    StorageFolder dir2 = null;

                    await semaphore.WaitAsync();
                    try {
                        dir2 = await folder.CreateFolderAsync(path, CreationCollisionOption.OpenIfExists);
                    }
                    finally {
                        semaphore.Release();
                    }

                    if (dir2 == null)
                        return false;

                    var item = await dir2.TryGetItemAsync(key_md5);
                    if (item != null) {
                        return true;
                    }
                }
                catch (Exception ex) {
                    Debug.WriteLine(ex.Message, "ERROR " + "__FileCache.isCached");
                }
            }

            return false;
        }

        public async Task save(string key, string data) {
            if (folder != null) {
                String key_md5 = Util.md5(key);
                String path = key_md5.Substring(0, 2);

                await semaphore.WaitAsync();
                try {
                    StorageFolder dir2 = await folder.CreateFolderAsync(path, CreationCollisionOption.OpenIfExists);

                    var file = await dir2.CreateFileAsync(key_md5, CreationCollisionOption.ReplaceExisting);
                    if (file != null) {
                        await FileIO.WriteTextAsync(file, data);
                    }
                }
                catch (Exception ex) {
                    Debug.WriteLine(ex.Message, "ERROR " + "__FileCache.save");
                }
                finally {
                    semaphore.Release();
                }
            }
        }
    }
}
