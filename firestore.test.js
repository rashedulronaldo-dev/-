const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ORG_ID = "kmnqhm_1995";
const ADMIN_UID = "admin_uid_1";
const TEACHER_UID = "teacher_uid_2";
const PARENT_UID = "parent_uid_3";
const STRANGER_UID = "stranger_uid_4";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function adminContext() {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: "rase2443@gmail.com",
    email_verified: true,
  });
}

function spoofedAdminContext() {
  return testEnv.authenticatedContext(STRANGER_UID, {
    email: "rase2443@gmail.com",
    email_verified: false,
  });
}

async function seedRoles() {
  const now = new Date(Date.now() - 5000);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("organizations").doc(ORG_ID).set({
      nameBn: "খেলার মাঠ নূরানী তালিমুল কুরআন ও হাফিজিয়া মাদরাসা",
      establishedYear: "১৯৯৫",
      currencySymbol: "৳",
      timezone: "Asia/Dhaka",
      academicYear: "২০২৬",
      ownerUid: ADMIN_UID,
      readerUids: [ADMIN_UID],
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("organizations").doc(ORG_ID).collection("members").doc(TEACHER_UID).set({
      uid: TEACHER_UID,
      orgId: ORG_ID,
      email: "teacher@madrasa.org",
      displayName: "মাওলানা আব্দুল করিম",
      role: "TEACHER",
      assignedClasses: ["হিফজ-ক"],
      linkedStudentIds: [],
      active: true,
      createdAt: now,
      updatedAt: now,
    });
    await db.collection("organizations").doc(ORG_ID).collection("members").doc(PARENT_UID).set({
      uid: PARENT_UID,
      orgId: ORG_ID,
      email: "parent@madrasa.org",
      displayName: "মোঃ রফিকুল ইসলাম",
      role: "PARENT",
      assignedClasses: [],
      linkedStudentIds: ["stu_1"],
      active: true,
      createdAt: now,
      updatedAt: now,
    });
  });
}

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("organizations").doc(ORG_ID).get());
  await assertFails(unauthDb.collection("organizations").doc(ORG_ID).collection("students").get());
  await assertFails(
    unauthDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_1").set({
      orgId: ORG_ID,
      fullName: "Test",
    })
  );
});

test("Email spoofing with email_verified=false cannot gain admin access", async () => {
  await seedRoles();
  const spoofDb = spoofedAdminContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(
    spoofDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_spoof").set({
      orgId: ORG_ID,
      studentCode: "KMN-99",
      fullName: "জাল শিক্ষার্থী",
      dateOfBirth: "2015-01-01",
      gender: "ছাত্র",
      fatherName: "পিতা",
      motherName: "মাতা",
      guardianPhone: "01700000000",
      address: "ঢাকা",
      admissionDate: "2026-01-01",
      department: "HIFZ",
      className: "হিফজ-ক",
      rollNumber: "1",
      emergencyContact: "01700000000",
      archived: false,
      createdByUid: STRANGER_UID,
      readerUids: [STRANGER_UID],
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Self-registration blocks non-admin from assigning ADMIN or TEACHER role", async () => {
  await seedRoles();
  const strangerDb = testEnv.authenticatedContext(STRANGER_UID, {
    email: "stranger@gmail.com",
    email_verified: true,
  }).firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(
    strangerDb.collection("organizations").doc(ORG_ID).collection("members").doc(STRANGER_UID).set({
      uid: STRANGER_UID,
      orgId: ORG_ID,
      email: "stranger@gmail.com",
      displayName: "Stranger",
      role: "ADMIN",
      assignedClasses: [],
      linkedStudentIds: [],
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    strangerDb.collection("organizations").doc(ORG_ID).collection("members").doc(STRANGER_UID).set({
      uid: STRANGER_UID,
      orgId: ORG_ID,
      email: "stranger@gmail.com",
      displayName: "অভিভাবক",
      role: "PARENT",
      assignedClasses: [],
      linkedStudentIds: [],
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Admin creates & updates student; ghost id field or invalid future timestamp fails", async () => {
  await seedRoles();
  const adminDb = adminContext().firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    adminDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_1").set({
      orgId: ORG_ID,
      studentCode: "KMN-1995-001",
      fullName: "আব্দুল্লাহ আল মামুন",
      dateOfBirth: "2014-05-12",
      gender: "ছাত্র",
      fatherName: "মোঃ রফিকুল ইসলাম",
      motherName: "রহিমা খাতুন",
      guardianPhone: "01711223344",
      guardianUid: PARENT_UID,
      address: "খেলার মাঠ, ঢাকা",
      admissionDate: "2026-01-10",
      department: "HIFZ",
      className: "হিফজ-ক",
      rollNumber: "০১",
      emergencyContact: "01711223344",
      archived: false,
      createdByUid: ADMIN_UID,
      readerUids: [ADMIN_UID, TEACHER_UID, PARENT_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Partial update succeeds
  await assertSucceeds(
    adminDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_1").update({
      className: "হিফজ-খ",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject ghost 'id' field on create
  await assertFails(
    adminDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_2").set({
      id: "stu_2",
      orgId: ORG_ID,
      studentCode: "KMN-1995-002",
      fullName: "যুবায়ের",
      dateOfBirth: "2015-01-01",
      gender: "ছাত্র",
      fatherName: "করিম",
      motherName: "সালমা",
      guardianPhone: "01811223344",
      address: "ঢাকা",
      admissionDate: "2026-01-10",
      department: "NOORANI",
      className: "নূরানী-১",
      rollNumber: "০২",
      emergencyContact: "01811223344",
      archived: false,
      createdByUid: ADMIN_UID,
      readerUids: [ADMIN_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject future timestamp
  await assertFails(
    adminDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_3").set({
      orgId: ORG_ID,
      studentCode: "KMN-1995-003",
      fullName: "হাসান",
      dateOfBirth: "2015-01-01",
      gender: "ছাত্র",
      fatherName: "করিম",
      motherName: "সালমা",
      guardianPhone: "01811223344",
      address: "ঢাকা",
      admissionDate: "2026-01-10",
      department: "NOORANI",
      className: "নূরানী-১",
      rollNumber: "০৩",
      emergencyContact: "01811223344",
      archived: false,
      createdByUid: ADMIN_UID,
      readerUids: [ADMIN_UID],
      createdAt: new Date(Date.now() + 60000),
      updatedAt: now,
    })
  );
});

test("Parent isolation: parent reads only linked student via readerUids query; unfiltered query fails", async () => {
  await seedRoles();
  const adminDb = adminContext().firestore();
  const now = new Date(Date.now() - 2000);
  await adminDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_1").set({
    orgId: ORG_ID,
    studentCode: "KMN-001",
    fullName: "আব্দুল্লাহ",
    dateOfBirth: "2014-05-12",
    gender: "ছাত্র",
    fatherName: "মোঃ রফিকুল ইসলাম",
    motherName: "রহিমা",
    guardianPhone: "01711223344",
    address: "ঢাকা",
    admissionDate: "2026-01-10",
    department: "HIFZ",
    className: "হিফজ-ক",
    rollNumber: "০১",
    emergencyContact: "01711223344",
    archived: false,
    createdByUid: ADMIN_UID,
    readerUids: [ADMIN_UID, TEACHER_UID, PARENT_UID],
    createdAt: now,
    updatedAt: now,
  });
  await adminDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_other").set({
    orgId: ORG_ID,
    studentCode: "KMN-002",
    fullName: "অন্য শিক্ষার্থী",
    dateOfBirth: "2014-05-12",
    gender: "ছাত্র",
    fatherName: "অন্য পিতা",
    motherName: "অন্য মাতা",
    guardianPhone: "01911223344",
    address: "ঢাকা",
    admissionDate: "2026-01-10",
    department: "NAZERA",
    className: "নাজেরা-১",
    rollNumber: "০২",
    emergencyContact: "01911223344",
    archived: false,
    createdByUid: ADMIN_UID,
    readerUids: [ADMIN_UID],
    createdAt: now,
    updatedAt: now,
  });

  const parentDb = testEnv.authenticatedContext(PARENT_UID).firestore();
  await assertSucceeds(
    parentDb.collection("organizations").doc(ORG_ID).collection("students")
      .where("readerUids", "array-contains", PARENT_UID).get()
  );
  await assertFails(parentDb.collection("organizations").doc(ORG_ID).collection("students").get());
  await assertFails(parentDb.collection("organizations").doc(ORG_ID).collection("students").doc("stu_other").get());
});

test("Teacher can record attendance & Quran progress; cannot create invoices or journal entries", async () => {
  await seedRoles();
  const teacherDb = testEnv.authenticatedContext(TEACHER_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    teacherDb.collection("organizations").doc(ORG_ID).collection("attendance").doc("att_1").set({
      orgId: ORG_ID,
      dateKey: "2026-11-18",
      personType: "STUDENT",
      personId: "stu_1",
      personName: "আব্দুল্লাহ",
      department: "HIFZ",
      className: "হিফজ-ক",
      status: "PRESENT",
      lateArrival: false,
      syncEventId: "sync_001",
      createdByUid: TEACHER_UID,
      readerUids: [ADMIN_UID, TEACHER_UID, PARENT_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    teacherDb.collection("organizations").doc(ORG_ID).collection("quran_progress").doc("qp_1").set({
      orgId: ORG_ID,
      studentId: "stu_1",
      studentName: "আব্দুল্লাহ",
      department: "HIFZ",
      dateKey: "2026-11-18",
      lessonType: "সবক",
      surahName: "সূরা আল-মুলক",
      paraNumber: 29,
      ayatRange: "১-১০",
      arabicSample: "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ",
      accuracyPercent: 96,
      mistakeCount: 1,
      weakLesson: false,
      approved: true,
      teacherComment: "মাশাআল্লাহ, তাজবিদ সুন্দর হয়েছে",
      createdByUid: TEACHER_UID,
      readerUids: [ADMIN_UID, TEACHER_UID, PARENT_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    teacherDb.collection("organizations").doc(ORG_ID).collection("journal_entries").doc("je_1").set({
      orgId: ORG_ID,
      entryDate: "2026-11-18",
      referenceNo: "JV-1",
      description: "Test",
      category: "EXPENSE",
      debitAccountCode: "5001",
      debitAccountName: "বেতন ব্যয়",
      creditAccountCode: "1001",
      creditAccountName: "নগদ",
      amount: 1000,
      totalDebit: 1000,
      totalCredit: 1000,
      status: "POSTED",
      entryType: "STANDARD",
      createdByUid: TEACHER_UID,
      readerUids: [TEACHER_UID],
      createdAt: now,
    })
  );
});

test("Double-entry accounting enforces debit == credit and strict immutability", async () => {
  await seedRoles();
  const adminDb = adminContext().firestore();
  const now = new Date(Date.now() - 2000);

  // Unbalanced entry must fail
  await assertFails(
    adminDb.collection("organizations").doc(ORG_ID).collection("journal_entries").doc("je_bad").set({
      orgId: ORG_ID,
      entryDate: "2026-11-18",
      referenceNo: "JV-BAD",
      description: "Unbalanced",
      category: "INCOME",
      debitAccountCode: "1001",
      debitAccountName: "নগদ",
      creditAccountCode: "4001",
      creditAccountName: "বেতন আয়",
      amount: 1500,
      totalDebit: 1500,
      totalCredit: 1000,
      status: "POSTED",
      entryType: "STANDARD",
      createdByUid: ADMIN_UID,
      readerUids: [ADMIN_UID],
      createdAt: now,
    })
  );

  // Balanced entry succeeds
  await assertSucceeds(
    adminDb.collection("organizations").doc(ORG_ID).collection("journal_entries").doc("je_ok").set({
      orgId: ORG_ID,
      entryDate: "2026-11-18",
      referenceNo: "JV-001",
      description: "মাসিক বেতন আদায়",
      category: "FEE_INCOME",
      debitAccountCode: "1001",
      debitAccountName: "নগদ তহবিল",
      creditAccountCode: "4001",
      creditAccountName: "বেতন আদায় আয়",
      amount: 1500,
      totalDebit: 1500,
      totalCredit: 1500,
      status: "POSTED",
      entryType: "STANDARD",
      createdByUid: ADMIN_UID,
      readerUids: [ADMIN_UID],
      createdAt: now,
    })
  );

  // Posted journal entry cannot be silently updated or deleted
  await assertFails(
    adminDb.collection("organizations").doc(ORG_ID).collection("journal_entries").doc("je_ok").update({
      amount: 2000,
    })
  );
  await assertFails(
    adminDb.collection("organizations").doc(ORG_ID).collection("journal_entries").doc("je_ok").delete()
  );
});

test("Client cannot mark online payment as VERIFIED directly", async () => {
  await seedRoles();
  const parentDb = testEnv.authenticatedContext(PARENT_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertFails(
    parentDb.collection("organizations").doc(ORG_ID).collection("payments").doc("pay_spoof").set({
      orgId: ORG_ID,
      receiptNumber: "REC-001",
      invoiceId: "inv_1",
      studentId: "stu_1",
      studentName: "আব্দুল্লাহ",
      amount: 1500,
      gateway: "BKASH",
      transactionId: "TRX123",
      status: "VERIFIED",
      sandboxMode: true,
      webhookVerified: true,
      createdByUid: PARENT_UID,
      readerUids: [ADMIN_UID, PARENT_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    parentDb.collection("organizations").doc(ORG_ID).collection("payments").doc("pay_pending").set({
      orgId: ORG_ID,
      receiptNumber: "REC-001",
      invoiceId: "inv_1",
      studentId: "stu_1",
      studentName: "আব্দুল্লাহ",
      amount: 1500,
      gateway: "BKASH",
      transactionId: "TRX123",
      status: "PENDING",
      sandboxMode: true,
      webhookVerified: false,
      createdByUid: PARENT_UID,
      readerUids: [ADMIN_UID, PARENT_UID],
      createdAt: now,
      updatedAt: now,
    })
  );
});
