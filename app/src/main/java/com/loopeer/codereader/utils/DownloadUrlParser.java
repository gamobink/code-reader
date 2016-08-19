package com.loopeer.codereader.utils;

import android.content.Context;
import android.text.TextUtils;

import com.loopeer.codereader.Navigator;
import com.loopeer.codereader.coreader.db.CoReaderDbHelper;
import com.loopeer.codereader.model.Repo;

import java.io.File;

public class DownloadUrlParser {
    private static final String GITHUB_REPO_URL_BASE = "https://codeload.github.com/";
    private static final String ZIP_SUFFIX = ".zip";

    public static void parseUrlAndDownload(Context context, String url) {
        String downloadUrl = DownloadUrlParser.parseGithubDownloadUrl(url);
        String repoName = DownloadUrlParser.getRepoName(url);
        Repo repo = new Repo(repoName
                , FileCache.getInstance().getRepoAbsolutePath(repoName), downloadUrl, true, 0);
        Repo sameRepo = CoReaderDbHelper.getInstance(context).readSameRepo(repo);
        long repoId;
        if (sameRepo != null) {
            repoId = Long.parseLong(sameRepo.id);
        } else {
            repoId = CoReaderDbHelper.getInstance(context).insertRepo(repo);
        }
        repo.id = String.valueOf(repoId);
        Navigator.startDownloadRepoService(context, downloadUrl, repo);
    }

    public static String parseGithubDownloadUrl(String url) {
        if (TextUtils.isEmpty(url)) return null;
        StringBuilder sb = new StringBuilder();
        String[] strings = url.split("/");
        if (strings.length < 5) return null;
        if (strings.length == 5) {
            sb.append(GITHUB_REPO_URL_BASE);
            sb.append(strings[3]);
            sb.append("/");
            if (strings[4].contains("?")) {
                String[] lastName = strings[4].split("\\?");
                sb.append(lastName[0]);
                sb.append("/");
            } else {
                sb.append(strings[4]);
                sb.append("/");
            }
            sb.append("zip/master");
            return sb.toString();
        }
        if (strings.length > 5) {
            sb.append(GITHUB_REPO_URL_BASE);
            sb.append(strings[3]);
            sb.append("/");
            sb.append(strings[4]);
            sb.append("/");
            sb.append("zip/master");
            return sb.toString();
        }
        return null;
    }

    public static File getRemoteRepoZipFileName(String repoName) {
        return new File(FileCache.getInstance().getCacheDir(), getRepoNameZip(repoName));
    }

    public static String getRepoNameZip(String name) {
        return name + ZIP_SUFFIX;
    }

    public static String getRepoName(String url) {
        String[] strings = url.split("/");
        return strings[4].split("//.")[0];
    }
}
