package com.ds.app.service;

import com.ds.app.entity.JiraAttachment;
import com.ds.app.entity.JiraComment;
import com.ds.app.entity.JiraIssue;
import com.ds.app.repository.JiraAttachmentRepository;
import com.ds.app.repository.JiraCommentRepository;
import com.ds.app.repository.JiraIssueRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.RFC4180ParserBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

	private final JiraIssueRepository issueRepository;
	private final JiraCommentRepository commentRepository;
	private final JiraAttachmentRepository attachmentRepository;
	private final UserMappingService userMappingService;

	@Value("${jira.csv-path}")
	private String csvPath;

	private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9]+-[0-9]+$");

	private static final Pattern ACCOUNT_PATTERN = Pattern.compile("\\[~accountid:([^\\]]+)\\]");

	private static final int BATCH_SIZE = 500;

	public void importAll() {
		log.info("========================================");
		log.info("JIRA CSV IMPORT STARTED");
		log.info("========================================");

		userMappingService.saveAllToDb();

		List<JiraIssue> issueBatch = new ArrayList<>();
		List<JiraComment> commentBatch = new ArrayList<>();
		List<JiraAttachment> attachmentBatch = new ArrayList<>();

		int rowNum = 0;
		int issueRowsSeen = 0;
		int issueRowsQueued = 0;
		int commentRowsQueued = 0;
		int attachmentRowsQueued = 0;
		int invalidIssueRows = 0;
		int continuationRowsUsed = 0;
		int parseFailedComments = 0;
		int parseFailedAttachments = 0;

		String lastIssueKey = null;
		String lastParentKey = null;
		String lastIssueType = null;
		String currentIssueBlock = null;

		Set<String> seenCommentsForCurrentIssue = new LinkedHashSet<>();
		Set<String> seenAttachmentsForCurrentIssue = new LinkedHashSet<>();

		try (CSVReader reader = buildReader()) {
			String[] headers = reader.readNext();

			if (headers == null) {
				log.error("CSV is empty!");
				return;
			}

			for (int i = 0; i < headers.length; i++) {
				if (headers[i] != null) {
					headers[i] = headers[i].replace("\uFEFF", "").trim();
				}
			}

			Map<String, Integer> singleColIdx = buildSingleColumnIndex(headers);
			Map<String, List<Integer>> multiColIdx = buildMultiColumnIndex(headers);

			List<Integer> commentIdxs = multiColIdx.getOrDefault("comment", Collections.emptyList());
			List<Integer> attachmentIdxs = multiColIdx.getOrDefault("attachment", Collections.emptyList());

			if (commentIdxs.isEmpty()) {
				log.warn("No 'Comment' columns found in CSV header.");
			}
			if (attachmentIdxs.isEmpty()) {
				log.warn("No 'Attachment' columns found in CSV header.");
			}

			String[] row;
			while ((row = reader.readNext()) != null) {
				rowNum++;

				String rawIssueKey = get(row, singleColIdx, "issue key");
				String rawParentKey = get(row, singleColIdx, "parent key");
				String rawIssueType = get(row, singleColIdx, "issue type");

				boolean isNewIssueRow = false;

				if (rawIssueKey != null && !rawIssueKey.isBlank()) {
					rawIssueKey = rawIssueKey.trim();

					if (!ISSUE_KEY_PATTERN.matcher(rawIssueKey).matches()) {
						invalidIssueRows++;
						continue;
					}

					lastIssueKey = s(rawIssueKey, 50);
					lastParentKey = (rawParentKey == null || rawParentKey.isBlank()) ? null
							: s(rawParentKey.trim(), 50);
					lastIssueType = (rawIssueType == null || rawIssueType.isBlank()) ? null
							: s(rawIssueType.trim(), 100); // ← ADDED
					isNewIssueRow = true;
					issueRowsSeen++;
				} else {
					if (lastIssueKey == null) {
						continue;
					}
					continuationRowsUsed++;
				}

				String issueKey = lastIssueKey;
				String parentKey = lastParentKey;
				String issueType = lastIssueType;

				if (!Objects.equals(currentIssueBlock, issueKey)) {
					currentIssueBlock = issueKey;
					seenCommentsForCurrentIssue.clear();
					seenAttachmentsForCurrentIssue.clear();
				}

				if (isNewIssueRow) {
					JiraIssue issue = JiraIssue.builder().issueKey(issueKey)
							.issueId(s(get(row, singleColIdx, "issue id"), 50)).issueType(issueType)
							.status(s(get(row, singleColIdx, "status"), 100))
							.statusCategory(s(get(row, singleColIdx, "status category"), 100))
							.priority(s(get(row, singleColIdx, "priority"), 50))
							.projectKey(s(get(row, singleColIdx, "project key"), 50))
							.projectName(s(get(row, singleColIdx, "project name"), 255))
							.reporter(s(get(row, singleColIdx, "reporter"), 255))
							.reporterId(s(get(row, singleColIdx, "reporter id"), 100))
							.assignee(s(get(row, singleColIdx, "assignee"), 255))
							.assigneeId(s(get(row, singleColIdx, "assignee id"), 100))
							.severity(s(get(row, singleColIdx, "custom field (severity)"), 100))
							.clientName(s(joinRepeatedColumns(row, headers, "custom field (client name)"), 255))
							.epicName(s(get(row, singleColIdx, "custom field (epic name)"), 255))
							.redmineTrackerId(s(get(row, singleColIdx, "custom field (redmine_tracker_id)"), 100))
							.ocrId(s(get(row, singleColIdx, "custom field (ocr_id)"), 100))
							.zohoDeskTicket(get(row, singleColIdx, "custom field (zoho desk ticket)"))
							.rootCause(get(row, singleColIdx, "custom field (root cause)"))
							.created(s(get(row, singleColIdx, "created"), 50))
							.updated(s(get(row, singleColIdx, "updated"), 50))
							.resolved(s(get(row, singleColIdx, "resolved"), 50))
							.startDate(s(get(row, singleColIdx, "custom field (start date)"), 50))
							.actualStartDate(s(get(row, singleColIdx, "custom field (actual start date)"), 50))
							.actualEndDate(s(get(row, singleColIdx, "custom field (actual end date)"), 50))
							.summary(s(get(row, singleColIdx, "summary"), 1000))
							.description(get(row, singleColIdx, "description"))
							.labels(joinRepeatedColumns(row, headers, "labels"))
							.components(joinRepeatedColumns(row, headers, "components"))
							.fixVersions(joinRepeatedColumns(row, headers, "fix versions"))
							.environment(get(row, singleColIdx, "environment"))
							.sprint(joinRepeatedColumns(row, headers, "sprint"))
							.issueReason(joinRepeatedColumns(row, headers, "custom field (issue reason (rca))"))
							.issueIn(joinRepeatedColumns(row, headers, "custom field (issue in)"))
							.releaseNotes(get(row, singleColIdx, "custom field (release note)")).parentKey(parentKey)
							.parentSummary(s(get(row, singleColIdx, "parent summary"), 1000))
							.storyPoints(s(get(row, singleColIdx, "custom field (story points)"), 50)).build();

					issueBatch.add(issue);
					issueRowsQueued++;
				}

				// Comments
				for (int idx : commentIdxs) {
					String rawComment = getSafe(row, idx);
					if (rawComment.isBlank())
						continue;

					String localCommentFingerprint = issueKey + "||" + rawComment;
					if (!seenCommentsForCurrentIssue.add(localCommentFingerprint))
						continue;

					try {
						JiraComment comment = parseComment(rawComment, issueKey);
						if (comment != null) {
							commentBatch.add(comment);
							commentRowsQueued++;
						} else {
							parseFailedComments++;
							log.warn("Row {} comment parse returned null for issue {}: {}", rowNum, issueKey,
									rawComment.length() > 80 ? rawComment.substring(0, 80) + "…" : rawComment);
						}
					} catch (Exception e) {
						parseFailedComments++;
						log.warn("Row {} comment parse threw for issue {}: {}", rowNum, issueKey, e.getMessage());
					}
				}

				// Attachments
				for (int idx : attachmentIdxs) {
					String rawAttachment = getSafe(row, idx);
					if (rawAttachment.isBlank())
						continue;

					String localAttachmentFingerprint = issueKey + "||" + rawAttachment;
					if (!seenAttachmentsForCurrentIssue.add(localAttachmentFingerprint))
						continue;

					try {
						// ← FIXED: issueType bhi pass karo
						JiraAttachment attachment = parseAttachment(rawAttachment, issueKey, parentKey, issueType);
						if (attachment != null) {
							attachmentBatch.add(attachment);
							attachmentRowsQueued++;
						} else {
							parseFailedAttachments++;
							log.warn("Row {} attachment parse returned null for issue {}: {}", rowNum, issueKey,
									rawAttachment.length() > 80 ? rawAttachment.substring(0, 80) + "…" : rawAttachment);
						}
					} catch (Exception e) {
						parseFailedAttachments++;
						log.warn("Row {} attachment parse threw for issue {}: {}", rowNum, issueKey, e.getMessage());
					}
				}

				if (issueBatch.size() >= BATCH_SIZE || commentBatch.size() >= BATCH_SIZE
						|| attachmentBatch.size() >= BATCH_SIZE) {
					flushBatches(issueBatch, commentBatch, attachmentBatch);
					log.info("Progress row={} issuesQueued={} commentsQueued={} attachmentsQueued={}", rowNum,
							issueRowsQueued, commentRowsQueued, attachmentRowsQueued);
				}
			}

			// Final flush — remaining records
			if (!issueBatch.isEmpty() || !commentBatch.isEmpty() || !attachmentBatch.isEmpty()) {
				flushBatches(issueBatch, commentBatch, attachmentBatch);
			}

			log.info("========================================");
			log.info("IMPORT COMPLETE");
			log.info("rowsProcessed={}", rowNum);
			log.info("issueRowsSeen={}", issueRowsSeen);
			log.info("issueRowsQueued={}", issueRowsQueued);
			log.info("commentRowsQueued={}", commentRowsQueued);
			log.info("attachmentRowsQueued={}", attachmentRowsQueued);
			log.info("invalidIssueRows={}", invalidIssueRows);
			log.info("continuationRowsUsed={}", continuationRowsUsed);
			log.info("parseFailedComments={}", parseFailedComments);
			log.info("parseFailedAttachments={}", parseFailedAttachments);
			log.info("========================================");

		} catch (Exception e) {
			log.error("Import failed at CSV read level: {}", e.getMessage(), e);
			throw new RuntimeException("CSV import failed", e);
		}
	}

	private CSVReader buildReader() throws Exception {
		FileReader fileReader = new FileReader(csvPath, StandardCharsets.UTF_8);

		ICSVParser parser = new RFC4180ParserBuilder().withSeparator(',').withQuoteChar('"').build();

		return new CSVReaderBuilder(fileReader).withCSVParser(parser).build();
	}

	@Transactional
	public void flushBatches(List<JiraIssue> issueBatch, List<JiraComment> commentBatch,
			List<JiraAttachment> attachmentBatch) {

		// ---------- Issues ----------
		if (!issueBatch.isEmpty()) {
			Map<String, JiraIssue> uniqueIssuesByKey = new LinkedHashMap<>();
			for (JiraIssue issue : issueBatch) {
				uniqueIssuesByKey.put(issue.getIssueKey(), issue);
			}

			Set<String> candidateIssueKeys = uniqueIssuesByKey.keySet();
			Set<String> existingIssueKeys = issueRepository.findExistingIssueKeys(candidateIssueKeys);

			List<JiraIssue> issuesToInsert = uniqueIssuesByKey.values().stream()
					.filter(i -> !existingIssueKeys.contains(i.getIssueKey())).collect(Collectors.toList());

			if (!issuesToInsert.isEmpty()) {
				issueRepository.saveAll(issuesToInsert);
			}
			issueBatch.clear();
		}

		// ---------- Comments ----------
		if (!commentBatch.isEmpty()) {
			Set<String> issueKeys = commentBatch.stream().map(JiraComment::getIssueKey).collect(Collectors.toSet());

			List<JiraComment> existingComments = issueKeys.isEmpty() ? Collections.emptyList()
					: commentRepository.findByIssueKeyIn(issueKeys);

			Set<String> existingCommentFingerprints = existingComments.stream().map(this::commentFingerprint)
					.collect(Collectors.toSet());

			Set<String> inBatchFingerprints = new LinkedHashSet<>();
			List<JiraComment> commentsToInsert = new ArrayList<>();

			for (JiraComment comment : commentBatch) {
				String fp = commentFingerprint(comment);
				if (existingCommentFingerprints.contains(fp))
					continue;
				if (!inBatchFingerprints.add(fp))
					continue;
				commentsToInsert.add(comment);
			}

			if (!commentsToInsert.isEmpty()) {
				commentRepository.saveAll(commentsToInsert);
			}
			commentBatch.clear();
		}

		// ---------- Attachments ----------
		if (!attachmentBatch.isEmpty()) {
			Set<String> issueKeys = attachmentBatch.stream().map(JiraAttachment::getIssueKey)
					.collect(Collectors.toSet());

			List<JiraAttachment> existingAttachments = issueKeys.isEmpty() ? Collections.emptyList()
					: attachmentRepository.findByIssueKeyIn(issueKeys);

			Set<String> existingAttachmentFingerprints = existingAttachments.stream().map(this::attachmentFingerprint)
					.collect(Collectors.toSet());

			Set<String> inBatchFingerprints = new LinkedHashSet<>();
			List<JiraAttachment> attachmentsToInsert = new ArrayList<>();

			for (JiraAttachment attachment : attachmentBatch) {
				String fp = attachmentFingerprint(attachment);
				// CHECK 1 — DB mein already hai?
				if (existingAttachmentFingerprints.contains(fp))
					continue;
				// CHECK 2 — Is batch mein already dekha?
				if (!inBatchFingerprints.add(fp))
					continue;
				// Dono checks pass — naya hai, insert karo
				attachmentsToInsert.add(attachment);
			}

			if (!attachmentsToInsert.isEmpty()) {
				attachmentRepository.saveAll(attachmentsToInsert);
			}
			attachmentBatch.clear();
		}
	}

	private JiraComment parseComment(String raw, String issueKey) {
		String[] parts = raw.split(";", 3);
		if (parts.length < 3)
			return null;

		String date = s(parts[0].trim(), 50);
		String userId = s(parts[1].trim(), 100);
		String text = replaceAccountIds(parts[2].trim());

		if (date == null || userId == null)
			return null;

		return JiraComment.builder().issueKey(issueKey).commentDate(date).userId(userId)
				.userName(userMappingService.resolveName(userId)).commentText(text).build();
	}

	private JiraAttachment parseAttachment(String raw, String issueKey, String parentKey, String issueType) {
		if (raw == null || raw.isBlank())
			return null;

		String normalized = raw.trim();

		if (normalized.startsWith("{") || normalized.startsWith("\"content\"")
				|| (normalized.toUpperCase().equals(normalized) && !normalized.contains("http"))) {
			return null;
		}

		String[] parts = normalized.split(";", -1);
		if (parts.length < 4)
			return null;

		String uploadedAt = s(parts[0].trim(), 50);
		String userId = s(parts[1].trim(), 100);

		// Find URL from end — supports filenames containing semicolons
		String url = null;
		int urlIndex = -1;
		for (int i = parts.length - 1; i >= 2; i--) {
			String candidate = parts[i].trim();
			if (candidate.startsWith("http")) {
				url = candidate;
				urlIndex = i;
				break;
			}
		}

		if (url == null)
			return null;

		StringBuilder filenameSb = new StringBuilder();
		for (int i = 2; i < urlIndex; i++) {
			if (i > 2)
				filenameSb.append(";");
			filenameSb.append(parts[i].trim());
		}

		String filename = filenameSb.toString().trim();
		if (filename.isBlank())
			return null;

		String ftpPath;
		if ("Epic".equalsIgnoreCase(issueType)) {
			// Epic's own attachments
			ftpPath = "/EPICS/" + issueKey + "/_epic_attachments/" + filename;

		} else if (parentKey != null && !parentKey.isBlank()) {
			// Child issue — nested inside parent epic
			ftpPath = "/EPICS/" + parentKey + "/" + issueKey + "/" + filename;

		} else {
			// Flat/orphan — no parent, not an epic
			ftpPath = "/NO_PARENT/" + issueKey + "/" + filename;
		}

		return JiraAttachment.builder().issueKey(issueKey).parentKey(parentKey).issueType(issueType)
				.uploadedAt(uploadedAt).userId(userId).userName(userMappingService.resolveName(userId))
				.filename(s(filename, 500)).jiraUrl(s(url, 1000)).ftpPath(s(ftpPath, 1000)).build();
	}

	private String replaceAccountIds(String text) {
		if (text == null)
			return null;

		Matcher matcher = ACCOUNT_PATTERN.matcher(text);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String resolvedName = userMappingService.resolveName(matcher.group(1));
			String replacement = "@" + resolvedName.replace("$", "\\$");
			matcher.appendReplacement(sb, replacement);
		}

		matcher.appendTail(sb);
		return sb.toString();
	}

	private String commentFingerprint(JiraComment c) {
		return safe(c.getIssueKey()) + "||" + safe(c.getCommentDate()) + "||" + safe(c.getUserId()) + "||"
				+ normalizeLongText(c.getCommentText());
	}

	private String attachmentFingerprint(JiraAttachment a) {
		return safe(a.getIssueKey()) + "||" + safe(a.getParentKey()) + "||" + safe(a.getUploadedAt()) + "||"
				+ safe(a.getUserId()) + "||" + safe(a.getFilename()) + "||" + safe(a.getJiraUrl());
	}

	private String normalizeLongText(String value) {
		if (value == null)
			return "";
		return value.trim().replace("\r\n", "\n").replace("\r", "\n");
	}

	private String joinRepeatedColumns(String[] row, String[] headers, String colNameLower) {
		List<String> values = new ArrayList<>();
		for (int i = 0; i < headers.length; i++) {
			if (headers[i] != null && headers[i].trim().toLowerCase().equals(colNameLower)) {
				String value = getSafe(row, i);
				if (!value.isBlank() && !values.contains(value)) {
					values.add(value);
				}
			}
		}
		return values.isEmpty() ? null : String.join(", ", values);
	}

	private Map<String, Integer> buildSingleColumnIndex(String[] headers) {
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			String h = headers[i] == null ? "" : headers[i].trim().toLowerCase();
			map.putIfAbsent(h, i);
		}
		return map;
	}

	private Map<String, List<Integer>> buildMultiColumnIndex(String[] headers) {
		Map<String, List<Integer>> map = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			String h = headers[i] == null ? "" : headers[i].trim().toLowerCase();
			map.computeIfAbsent(h, k -> new ArrayList<>()).add(i);
		}
		return map;
	}

	private String get(String[] row, Map<String, Integer> colIdx, String colNameLower) {
		Integer idx = colIdx.get(colNameLower);
		if (idx == null || idx < 0 || idx >= row.length)
			return null;
		String val = row[idx];
		if (val == null)
			return null;
		val = val.trim();
		return val.isBlank() ? null : val;
	}

	private String getSafe(String[] row, int idx) {
		if (idx < 0 || idx >= row.length)
			return "";
		return row[idx] == null ? "" : row[idx].trim();
	}

	private String s(String val, int maxLen) {
		if (val == null)
			return null;
		val = val.trim();
		return val.length() > maxLen ? val.substring(0, maxLen) : val;
	}

	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
}